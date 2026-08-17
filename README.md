# Gym REST API — Trainer/Trainee/Training Management

Spring Boot REST API on top of the plain-Hibernate DAO/service layer from the previous module
(`dao`/`service`/`entity` are untouched in their transaction handling — they're wired into Spring
as beans in [`BeanConfiguration`](src/main/java/com/epam/gym/config/BeanConfiguration.java) rather
than rewritten).

## Quick start

```bash
mvn spring-boot:run                                    # start on :8080 with the "local" profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev      # ...or dev/stg/prod (see Environments below)
mvn clean test                                          # run unit + integration tests (in-memory H2)
mvn clean verify                                        # tests + build the executable jar + JaCoCo report
```

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
Actuator: http://localhost:8080/actuator

## Environments (Spring profiles)

Four profiles, each with its own `application-{profile}.yml` — `spring.datasource.*` (connection)
and `app.hibernate.*` (dialect/ddl-auto/show-sql, bound by
[`HibernateProperties`](src/main/java/com/epam/gym/config/HibernateProperties.java)) differ per
environment; [`BeanConfiguration.sessionFactory`](src/main/java/com/epam/gym/config/BeanConfiguration.java)
builds the native Hibernate `SessionFactory` from whichever `DataSource` Spring Boot autoconfigures
for the active one:

| Profile | Database | ddl-auto | Actuator exposure |
|---|---|---|---|
| `local` (default) | H2 file, `./data/gymdb-local` | `update` | `*` (everything) |
| `dev` | H2 file, `./data/gymdb-dev` | `update` | `health,info,metrics,prometheus,env,beans` |
| `stg` | PostgreSQL (`DB_HOST`/`DB_USERNAME`/`DB_PASSWORD` env vars) | `validate` | `health,info,metrics,prometheus` |
| `prod` | PostgreSQL, credentials **required** from the environment (no defaults — fails fast rather than connecting with a placeholder) | `validate` | `health,prometheus` only |

Select one with `SPRING_PROFILES_ACTIVE=dev` (env var) or `--spring.profiles.active=dev` (arg); with
neither set, `local` runs. The constant Training Type list (Cardio, Strength, Yoga, CrossFit,
Stretching, Zumba) is seeded on first startup by
[`TrainingTypeSeeder`](src/main/java/com/epam/gym/config/TrainingTypeSeeder.java) regardless of
profile, since there's no REST endpoint to create one (task note: the table "could not be updated
from the application").

## Actuator: health & metrics

Enabled via `spring-boot-starter-actuator`, on top of the standard endpoints:

- **Custom health indicators** (`GET /actuator/health`):
  [`DatabaseHealthIndicator`](src/main/java/com/epam/gym/health/DatabaseHealthIndicator.java) runs a
  trivial query through the same `TransactionExecutor` every DAO call uses, and
  [`TrainingTypesHealthIndicator`](src/main/java/com/epam/gym/health/TrainingTypesHealthIndicator.java)
  reports DOWN if the constant reference list is empty (a misconfigured/unseeded environment would
  otherwise only surface as failed trainer registrations later).
- **Custom Prometheus metrics** (`GET /actuator/prometheus`), via
  [`GymMetrics`](src/main/java/com/epam/gym/metrics/GymMetrics.java): counters
  `gym_trainee_registrations_total`, `gym_trainer_registrations_total`, `gym_trainings_added_total`,
  `gym_login_attempts_total{result=success|failure}`, and a gauge `gym_training_types` reflecting the
  current size of the reference list.

## Authentication

Every endpoint except the two registrations (`POST /api/trainees`, `POST /api/trainers`) and Login
(`GET /api/auth/login`, which *is* the authentication check) requires **HTTP Basic auth**
(`Authorization: Basic base64(username:password)`). Change Login authenticates via its own
`oldPassword` field instead.

Basic-auth credentials are resolved by
[`AuthCredentialsArgumentResolver`](src/main/java/com/epam/gym/web/security/AuthCredentialsArgumentResolver.java)
into an `AuthCredentials` controller-method parameter. Missing/malformed headers are rejected there;
whether the header's username/password actually match the *target* resource (the trainee/trainer in
the path) is enforced by the existing service-layer `authenticate(username, password)` checks — so a
trainee can only ever act on their own profile, and a trainer can only ever add trainings under their
own credentials.

## REST endpoints

| # | Functionality | Method & path |
|---|---|---|
| 1 | Trainee Registration | `POST /api/trainees` |
| 2 | Trainer Registration | `POST /api/trainers` |
| 3 | Login | `GET /api/auth/login?username=&password=` |
| 4 | Change Login | `PUT /api/auth/password` |
| 5 | Get Trainee Profile | `GET /api/trainees/{username}` |
| 6 | Update Trainee Profile | `PUT /api/trainees/{username}` |
| 7 | Delete Trainee Profile | `DELETE /api/trainees/{username}` |
| 8 | Get Trainer Profile | `GET /api/trainers/{username}` |
| 9 | Update Trainer Profile | `PUT /api/trainers/{username}` |
| 10 | Get active trainers not assigned to a trainee | `GET /api/trainees/{username}/unassigned-trainers` |
| 11 | Update Trainee's Trainer List | `PUT /api/trainees/{username}/trainers` |
| 12 | Get Trainee Trainings List | `GET /api/trainees/{username}/trainings?periodFrom=&periodTo=&trainerName=&trainingType=` |
| 13 | Get Trainer Trainings List | `GET /api/trainers/{username}/trainings?periodFrom=&periodTo=&traineeName=` |
| 14 | Add Training | `POST /api/trainings` |
| 15 | Activate/De-Activate Trainee | `PATCH /api/trainees/{username}/status` |
| 16 | Activate/De-Activate Trainer | `PATCH /api/trainers/{username}/status` |
| 17 | Get Training Types | `GET /api/training-types` |

Notes on a couple of deliberate choices:
- **Add Training has no `trainingType` field.** The task's request payload doesn't list one, and a
  training's type follows directly from the assigned trainer's own specialization
  (`TrainingServiceImpl.addTraining` derives it from `trainer.getSpecialization()`), so there's
  nothing else for the field to mean.
- **PATCH is not idempotent, by design.** Activate/de-activate rejects a request for the account's
  current state (`IllegalStateTransitionException` → `409 Conflict`) per the task's explicit note
  that this action is not idempotent — everything else (GET/PUT/DELETE) is.
- **Update Trainee/Trainer Profile's `isActive` is a plain field set**, not routed through the
  not-idempotent activate/de-activate path — it's part of a full profile replace via PUT.

## Error handling

[`GlobalExceptionHandler`](src/main/java/com/epam/gym/web/error/GlobalExceptionHandler.java) maps
every failure to a consistent JSON body (`timestamp`, `status`, `error`, `message`, `path`,
`transactionId`, optional `validationErrors`) and the matching HTTP status:
`ValidationException`/bean-validation failures → 400, `AuthenticationException` → 401,
`EntityNotFoundException` → 404, `IllegalStateTransitionException` → 409, anything unexpected → 500.

## Logging

Two levels, per the task spec:
1. **Transaction level** — [`TransactionIdFilter`](src/main/java/com/epam/gym/web/logging/TransactionIdFilter.java)
   assigns a `transactionId` (reused from an incoming `X-Transaction-Id` header if present, otherwise
   generated) to every request, puts it in the SLF4J MDC for the request's lifetime, and echoes it
   back on the response — every log line during that request carries it (`[txId=...]` in the pattern).
2. **REST call level** — [`RequestLoggingAspect`](src/main/java/com/epam/gym/web/logging/RequestLoggingAspect.java)
   logs which endpoint was called, the request (with any `*password*`-named parameter or JSON field
   masked to `***` — see its tests for the exact cases covered), and the outcome (`200 OK` or the
   exception).

## API documentation

Controllers are annotated with Swagger 2 annotations (`@Api`, `@ApiOperation`, `@ApiParam`,
`@ApiImplicitParam(s)`, `@ApiResponses`) from `io.swagger:swagger-annotations`. The live interactive
UI is generated by **springdoc** (OpenAPI 3) rather than springfox, since springfox has no support
for Spring Boot 3 / the `jakarta.*` namespace that this project's entities already use.

## Design notes carried over from the previous module

- **Transactions**: every service method still runs through `TransactionExecutor.executeInTransaction(...)`.
- **Cascade delete**: deleting a trainee cascades to their trainings (task note).
- **Username generation**: `firstname.lastname`, lower-cased, with a numeric suffix on collision;
  password is a random 10-character alphanumeric string (`CredentialGenerator`).
- **Trainees ↔ Trainers**: many-to-many; **Users ↔ Trainee/Trainer**: one-to-one, parent/child.

## Tests

`mvn clean verify` runs unit tests (services, DTOs, entities, config, logging, auth, health
indicators, metrics) plus MockMvc slice tests for all five controllers (happy path, validation
errors, auth failures, not-found, conflict), one full-stack DAO/service integration test against
real Hibernate + in-memory H2, and one `@SpringBootTest` (`ActuatorIntegrationTest`) that boots the
whole context on the `test` profile and hits `/actuator/health` and `/actuator/prometheus` for real.
JaCoCo report: `target/site/jacoco/index.html` (line coverage ~94%, well above the 80% bar).

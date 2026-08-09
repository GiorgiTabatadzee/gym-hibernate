# Gym REST API — Trainer/Trainee/Training Management

Spring Boot REST API on top of the plain-Hibernate DAO/service layer from the previous module
(`dao`/`service`/`entity` are untouched in their transaction handling — they're wired into Spring
as beans in [`BeanConfiguration`](src/main/java/com/epam/gym/config/BeanConfiguration.java) rather
than rewritten).

## Quick start

```bash
mvn spring-boot:run             # start the API on :8080
mvn clean test                  # run unit + integration tests (in-memory H2)
mvn clean verify                # tests + build the executable jar + JaCoCo report
```

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

## Database

Default: **H2**, file-based, stored at `./data/gymdb`, schema auto-managed
(`hibernate.hbm2ddl.auto=update`). Zero setup required. The constant Training Type list (Cardio,
Strength, Yoga, CrossFit, Stretching, Zumba) is seeded on first startup by
[`TrainingTypeSeeder`](src/main/java/com/epam/gym/config/TrainingTypeSeeder.java), since there's no
REST endpoint to create one (task note: the table "could not be updated from the application").

To switch to **PostgreSQL**: edit `src/main/resources/hibernate.cfg.xml` — the four connection
properties and the dialect line have the replacement values commented directly above them.

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

`mvn clean verify` runs unit tests (services, DTOs, entities, config, logging, auth) plus MockMvc
slice tests for all five controllers (happy path, validation errors, auth failures, not-found,
conflict) and one full-stack integration test against real Hibernate + in-memory H2. JaCoCo report:
`target/site/jacoco/index.html` (line coverage well above the 80% bar).

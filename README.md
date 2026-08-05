# Gym Hibernate — Trainer/Trainee/Training Management

Plain Hibernate (no Spring) implementation of the DB schema and 18
functionalities from the task sheet, built on top of the JPA (`jakarta.persistence`)
annotations and the native Hibernate `Session` API.

## Quick start

```bash
mvn clean test              # run unit + integration tests (uses in-memory H2)
mvn clean package           # build the jar
mvn compile exec:java -Dexec.mainClass=com.epam.gym.Application   # run the demo
```

Or run `com.epam.gym.Application` directly from your IDE (IntelliJ: right-click →
Run). It walks through all 18 functionalities against a real file-based H2
database and logs each step.

**Note on this environment:** the sandbox I built this in can't reach Maven
Central, so I couldn't run `mvn` itself here. Instead I hand-wrote stub jars
matching the exact API surface of Hibernate 6 / jakarta.persistence / SLF4J /
JUnit5 / Mockito and compiled the entire `src/main` and `src/test` tree
against them with `javac` directly — that catches real type errors, wrong
method signatures, and generics mistakes (and it did catch one: an
`assertEquals(int, Integer)` overload ambiguity, now fixed). It does not
replace a real `mvn clean test` run on your machine, which I'd still recommend
as the first thing you do.

## Database

Default: **H2**, file-based, stored at `./data/gymdb`, schema auto-managed
(`hibernate.hbm2ddl.auto=update`) — so first run creates everything and later
runs keep your data. Zero setup required.

To switch to **PostgreSQL**: edit `src/main/resources/hibernate.cfg.xml` —
the four connection properties and the dialect line have the replacement
values commented directly above them. The `postgresql` JDBC driver is already
on the classpath via `pom.xml`.

## Project layout

```
entity/    User, Trainee, Trainer, Training, TrainingType — JPA-mapped per the schema
dao/       Hibernate Session-based data access, one interface + impl per entity
service/   Business logic: the 18 functionalities, auth checks, validation
dto/       Search-criteria and request payloads (TraineeTrainingCriteria, etc.)
util/      HibernateUtil (SessionFactory), TransactionExecutor (tx boundaries),
           CredentialGenerator (username/password generation)
exception/ AuthenticationException, EntityNotFoundException, ValidationException,
           IllegalStateTransitionException
Application.java   demo/smoke-test driver exercising the full flow
```

## Assumption flagged up front: credential generation

I didn't have your previous module's codebase, so `CredentialGenerator`
reimplements a standard, commonly-used scheme rather than your exact one:

- **username** = `firstname.lastname` (lower-cased); on collision, the
  smallest free serial suffix is appended (`john.doe`, then `john.doe1`,
  `john.doe2`, ...)
- **password** = random 10-character alphanumeric string

Every caller goes through this one class, so if your actual generator works
differently, that's the only file to change.

## Functionality → code map

| # | Functionality | Where |
|---|---|---|
| 1 | Create Trainer profile | `TrainerService.createTrainerProfile` |
| 2 | Create Trainee profile | `TraineeService.createTraineeProfile` |
| 3 | Trainee username/password matching | `TraineeService.matchCredentials` |
| 4 | Trainer username/password matching | `TrainerService.matchCredentials` |
| 5 | Select Trainer profile by username | `TrainerService.getProfileByUsername` |
| 6 | Select Trainee profile by username | `TraineeService.getProfileByUsername` |
| 7 | Trainee password change | `TraineeService.changePassword` |
| 8 | Trainer password change | `TrainerService.changePassword` |
| 9 | Update trainer profile | `TrainerService.updateProfile` |
| 10 | Update trainee profile | `TraineeService.updateProfile` |
| 11 | Activate/de-activate trainee | `TraineeService.setActive` |
| 12 | Activate/de-activate trainer | `TrainerService.setActive` |
| 13 | Delete trainee by username | `TraineeService.deleteProfileByUsername` |
| 14 | Trainee trainings list + criteria | `TraineeService.getTrainingsList` |
| 15 | Trainer trainings list + criteria | `TrainerService.getTrainingsList` |
| 16 | Add training | `TrainingService.addTraining` |
| 17 | Trainers not assigned to a trainee | `TraineeService.getTrainersNotAssigned` |
| 18 | Update trainee's trainers list | `TraineeService.updateTrainersList` |

Every operation except the two `create*Profile` methods requires the
caller's own username + password, checked before the operation runs (task
note #2). `TrainingType` is create/read-only — there is deliberately no
update method (task note #12).

## Design notes

- **Transactions**: every service method runs through
  `TransactionExecutor.executeInTransaction(...)`, which opens a Session,
  begins a Transaction, commits on success, and rolls back + rethrows on any
  `RuntimeException`. This is the single place transaction boundaries live —
  no service method opens a Session itself.
- **Testability**: `TransactionExecutor` is an interface specifically so unit
  tests can swap in `FakeTransactionExecutor` (runs the work function against
  a plain Mockito mock `Session`) instead of standing up real Hibernate. DAOs
  are mocked directly in service tests; `GymIntegrationTest` is the one test
  that exercises real Hibernate + H2 end to end, including the cascade
  delete.
- **Cascade delete**: `Trainee.trainings` is `cascade = ALL, orphanRemoval =
  true`, so `deleteProfileByUsername` cascades to the trainee's `Training`
  rows automatically (task note #7). `Trainer` has no such cascade — trainer
  deletion isn't in scope.
- **Not idempotent activate/deactivate**: `setActive` throws
  `IllegalStateTransitionException` if the account is already in the
  requested state, per task note #6.
- **Logging**: SLF4J/Logback, INFO for business events, WARN for failed
  auth/rollback, `org.hibernate` pinned to WARN to keep the console readable
  (flip to DEBUG in `logback.xml` to see generated SQL). Passwords are never
  logged — `User.toString()` deliberately omits the field.
- **Passwords are stored in plaintext** to match the plain generated-string
  scheme from the previous module. For anything beyond a coursework exercise
  you'd hash them (e.g. BCrypt) before persisting — flagging this so it's a
  conscious choice, not an oversight.

## Tests

- `CredentialGeneratorTest` — pure logic, no mocking needed
- `TraineeServiceImplTest`, `TrainerServiceImplTest`, `TrainingServiceImplTest`
  — Mockito-based, cover the happy path, validation failures, auth failures,
  and the not-idempotent activate/deactivate rule for every functionality
- `GymIntegrationTest` — one full lifecycle test against real in-memory H2:
  create → authenticate → add training → list trainings → assign trainer →
  hard-delete → verify cascade

Run `mvn test jacoco:report` and open
`target/site/jacoco/index.html` for the coverage report (rubric asks for
≥80% line coverage).

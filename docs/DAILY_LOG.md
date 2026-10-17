# Daily Log

## 2026-10-11

Work performed:

- Inspected the new workspace.
- Verified Java 21, Maven, and Git.
- Confirmed the workspace is not currently a Git repository.
- Extracted Project 1 requirements from the official PDF using Apache PDFBox.
- Created the initial Spring Boot project.
- Implemented the health endpoint.
- Created initial documentation and tracking files.
- Added `.gitignore`.
- Initialized a local Git repository on branch `main`.
- Checked local Git signing settings; no signing key, `commit.gpgsign`, or `gpg.format` is configured yet.

Files changed:

- `pom.xml`
- `src/main/java/com/zaalima/orchestrator/OrchestratorApplication.java`
- `src/main/java/com/zaalima/orchestrator/api/HealthController.java`
- `src/main/java/com/zaalima/orchestrator/api/dto/HealthResponse.java`
- `src/main/java/com/zaalima/orchestrator/config/BuildPropertiesConfig.java`
- `src/main/resources/application.yml`
- `src/test/java/com/zaalima/orchestrator/api/HealthControllerTest.java`
- `docs/OFFICIAL_REQUIREMENTS.md`
- `docs/TEAM_RESPONSIBILITIES.md`
- `docs/REQUIREMENTS_MATRIX.md`
- `docs/PROJECT_PLAN.md`
- `docs/ARCHITECTURE.md`
- `docs/INTEGRATION_CONTRACTS.md`
- `docs/PROGRESS.md`
- `docs/DAILY_LOG.md`
- `docs/TEST_REPORT.md`
- `.gitignore`

Tests executed:

- `mvn test` - BUILD SUCCESS, 1 test run, 0 failures, 0 errors, 0 skipped.

Remaining work:

- Continue core backend implementation with task APIs and orchestration contracts.
- Ask before creating or connecting a GitHub remote.
- Configure commit signing before making official signed commits.

## 2026-10-13

Work Performed:

- Created Agent and Agent Registry java files
- Organized the file structure
- This Agent Registry file is used to interact with the core orchestrator and manage the virtual thread registration

Remaining work:

- Test the Agent files. Hope it will be completed by tomorrow 14-10-2026

## 2026-10-15

Work performed:

- Verified active C:\IMP project on fix/application-startup at Ahamed's ab0eff8. OneDrive folder is a separate older copy.
- Reviewed his commit; full startup failed despite the MVC test passing. GitHub reported no associated PR and an unsigned commit.
- Preserved Agent.java/AgentRegistry.java. Removed duplicate controller/HealthController.java and corrected the health test import.
- Added task submission/status/cancellation, immutable snapshots, UTC timestamps, validation, and centralized problem responses.
- Added configurable bounded storage; no execution, persistence, or distributed integration is claimed.
- Proposed discovery/client/execution Java ports and documented Redis dependencies and a simulated-agent test MVP.
- Updated architecture, contracts, requirements, plan, ownership, progress, and tests. No work or commits were backdated.

Files changed:

- src/main/java/com/zaalima/orchestrator/controller/HealthController.java (duplicate removed)
- src/main/java/com/zaalima/orchestrator/api/{TaskController,ApiExceptionHandler}.java
- src/main/java/com/zaalima/orchestrator/api/dto/{SubmitTaskRequest,TaskResponse}.java
- src/main/java/com/zaalima/orchestrator/core/{Task,TaskStatus,TaskService,TaskNotFoundException,TaskCapacityException}.java
- src/main/java/com/zaalima/orchestrator/config/{TaskConfiguration,TaskProperties}.java
- src/main/java/com/zaalima/orchestrator/integration/{AgentDirectory,AgentClient,AgentExecutionResult,TaskExecutionEngine}.java
- src/main/resources/application.yml
- src/test/java/com/zaalima/orchestrator/api/{HealthControllerTest,TaskApiIntegrationTest,TaskCapacityIntegrationTest}.java
- src/test/java/com/zaalima/orchestrator/core/TaskServiceTest.java
- README.md and docs/{ARCHITECTURE,INTEGRATION_CONTRACTS,PROGRESS,PROJECT_PLAN,REQUIREMENTS_MATRIX,TEAM_RESPONSIBILITIES,DAILY_LOG,TEST_REPORT}.md

Tests executed:

- mvn -B -ntp verify: BUILD SUCCESS; 19 tests, zero failures/errors/skips; executable JAR packaged in the isolated copy.
- Real HTTP tests cover full startup, health, submission/read/cancel, validation, malformed JSON/UUID, 404, and storage capacity 503.
- Functional concurrency check: 60 attempts on 8 test threads, exactly 10 accepted with capacity 10; not a connection benchmark.
- Active-project verification: mvn -B -ntp clean verify -Ddebug=false -Dlogging.level.org.springframework=INFO; BUILD SUCCESS at 15:24 IST, 19 tests, zero failures/errors/skips. Packaged JAR built in C:\IMP project.
- Packaged-JAR smoke test on temporary localhost port 62579: health UP, submitted/retrieved QUEUED, cancelled CANCELLED. Temporary process stopped.
- First active-project verification had 10 context errors because the controller deletion was omitted during patch transfer; applied that deletion, then clean verification passed. No unresolved build failure remains.

Remaining work:

- Review uncommitted active-project changes and configure signing before user-controlled commits/PR. No commit, push, merge, or remote change performed.
- Dispatch, execution lifecycle, results/aggregation, and approved teammate integration.
- Week 3/4 work remains UNASSIGNED until the lead decides ownership.

## 2026-10-17

- Implemented `InMemoryAgentRegistry in the domain package.
- Registration is idempotent by agent name: registering the same name returns the existing agent.
- Used a `ReentrantLock` to guard the id and name indexes together. I chose it over
  `synchronized` so virtual threads are never pinned.
- Heartbeats are lock-free and cannot resurrect a removed agent.
- Wrote `InMemoryAgentRegistryTest` with 12 tests, including concurrency tests on virtual
  threads (5,000 same-name registrations produce one agent; 16 threads of
  register/heartbeat/remove churn never fail).

**Results**
- `./mvnw test`: [8 passed, 0 failed]

**Problems and fixes**
- My first draft used `computeIfAbsent` over two maps. A concurrent remove and register
  could return null for a name that was mid-removal. Fixed with a lock around both
  indexes and added a churn test to guard against regression.


# Requirements Matrix

| Requirement | Assigned Member | Status | Evidence |
| --- | --- | --- | --- |
| Verify workspace and toolchain | HARI | COMPLETED | Java 21.0.12.1, Maven 3.9.16, Git 2.52.0 checked on 2026-10-11 |
| Extract Project 1 PDF requirements | HARI | COMPLETED | Extracted with Apache PDFBox on 2026-10-11 |
| Create Spring Boot 3 backend | HARI | COMPLETED | `pom.xml`, `OrchestratorApplication` |
| Enable Java 21 Virtual Threads configuration | HARI / Robin integration | IN PROGRESS | `spring.threads.virtual.enabled=true`; production execution engine belongs to Robin |
| Implement health endpoint | HARI | COMPLETED | `GET /api/v1/health`, `HealthControllerTest` |
| Implement core REST APIs | HARI | IN PROGRESS | POST/GET/DELETE task APIs verified; result API pending |
| Implement request/response DTOs | HARI | IN PROGRESS | Health and task DTOs verified; result DTOs pending |
| Implement task submission validation | HARI | COMPLETED | HTTP tests cover invalid capability, null/oversized payload, blank key, malformed JSON/UUID |
| Implement core orchestration service | HARI | IN PROGRESS | TaskService handles admission/lookup/cancel; dispatch/aggregation pending |
| Implement task submission and status handling | HARI | COMPLETED | TaskApiIntegrationTest verifies POST 202, Location, GET, and unknown IDs |
| Implement task lifecycle management | HARI | IN PROGRESS | QUEUED/CANCELLED implemented; execution states pending |
| Implement result aggregation | HARI | NOT STARTED | Pending design |
| Define integration interfaces | HARI | IN PROGRESS | AgentDirectory, AgentClient, TaskExecutionEngine proposed; Redis/team agreement pending |
| Implement centralized exception handling for current APIs | HARI | COMPLETED | ApiExceptionHandler and HTTP 400/404/503 tests |
| Implement agent registration and communication | Ahamed | WAITING FOR TEAMMATE | ab0eff8 adds model/registry interface; implementation/security/endpoints/tests pending |
| Implement Virtual Thread execution engine | Robin | WAITING FOR TEAMMATE | Interface contract pending |
| Implement Structured Concurrency | Robin | WAITING FOR TEAMMATE | Java 21 preview configuration pending |
| Implement Redis Pub/Sub | Navitha | WAITING FOR TEAMMATE | Interface contract pending |
| Implement distributed locking | Navitha | WAITING FOR TEAMMATE | Interface contract pending |
| Validate 10,000+ concurrent connection target | UNASSIGNED | UNASSIGNED | Requires benchmark plan and measured result |
| Implement least-utilized-node load balancing | UNASSIGNED | UNASSIGNED | Not assigned |
| Integrate Resilience4j circuit breakers | UNASSIGNED | UNASSIGNED | Not assigned |
| Integrate Resilience4j bulkheads | UNASSIGNED | UNASSIGNED | Not assigned |
| Test Virtual Thread pinning behavior | UNASSIGNED | UNASSIGNED | Not assigned |
| Investigate memory leaks and resource usage | UNASSIGNED | UNASSIGNED | Not assigned |
| Prepare Docker Compose deployment | UNASSIGNED | UNASSIGNED | Not assigned |
| Benchmark Virtual Threads vs platform threads | UNASSIGNED | UNASSIGNED | Not assigned |
| Develop live terminal dashboard | UNASSIGNED | UNASSIGNED | Not assigned |
| Complete final documentation | HARI / Team | IN PROGRESS | Initial docs created |
| Create GitHub repository | HARI / Team | COMPLETED | Existing origin and October 11/13 commits verified |
| Push daily signed commits | HARI / Team | IN PROGRESS | Contributions exist; ab0eff8 unsigned; daily gaps and signing/hooks unresolved |
| Deliver high-concurrency benchmarking report | UNASSIGNED | UNASSIGNED | Final deliverable from official PDF |
| Deliver final architectural evaluation roadmap | UNASSIGNED | UNASSIGNED | Final review requirement from official PDF |
| Restore full application startup | HARI | COMPLETED | Duplicate controller removed; full-context HTTP tests pass |
| Bound process-local task storage | HARI | COMPLETED | 60 attempts: exactly 10 admitted at capacity 10; HTTP 503 verified |

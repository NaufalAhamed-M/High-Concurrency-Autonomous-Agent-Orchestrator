# Architecture

Updated: 2026-10-15.

## Implemented Components

- api: one health controller, task controller, centralized MVC/task exception handling.
- api.dto: validated task request and task/health response DTOs.
- core: immutable snapshots, QUEUED/CANCELLED lifecycle, submission/lookup/cancellation service.
- config: UTC clock, validated task capacity, Spring Boot virtual-thread setting.
- agent: Ahamed's model and registry interface, retained unchanged.
- integration: proposed discovery/client/execution ports; production adapters are not wired.

## Request Flow

1. POST /api/v1/tasks validates capability and string payload.
2. TaskService reserves capacity without blocking and stores an immutable QUEUED task in a ConcurrentHashMap.
3. Controller returns HTTP 202 with Location and a task snapshot.
4. GET returns a snapshot or HTTP 404.
5. DELETE atomically transitions QUEUED to CANCELLED. Repeated cancellation preserves the first cancellation snapshot.

Capacity applies to all stored records, including cancelled tasks. Records are not evicted, disappear at restart, and are not shared across instances. This is an in-memory API foundation, not a distributed task queue.

## Future Execution Flow

After contracts/adapters are approved, orchestration will discover agents through AgentDirectory, submit work through TaskExecutionEngine, invoke AgentClient, and aggregate results. Execution states, deadlines, running-task cancellation, and result APIs remain pending. Redis interfaces/event schemas require agreement with Navitha.

## Verification and Boundaries

Real HTTP RANDOM_PORT SpringBootTest suites exercise full component scanning and catch the duplicate-controller regression. Service tests check immutable payloads, cancellation, lookup, and concurrent capacity admission.

The API binds to localhost by default because authentication is pending. No pinning, leak, cluster, or 10,000-connection guarantee has been established.

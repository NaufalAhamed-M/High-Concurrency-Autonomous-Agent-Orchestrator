# Integration Contracts

Updated: 2026-10-15. Java ports are proposals for team review. No production teammate adapter is wired yet.

## Implemented REST APIs

| Method | Path | Success | Behavior |
| --- | --- | --- | --- |
| GET | /api/v1/health | 200 | Service status, name, version, UTC timestamp |
| POST | /api/v1/tasks | 202 | Store a QUEUED task; Location header identifies its status URL |
| GET | /api/v1/tasks/{taskId} | 200 | Return a task snapshot |
| DELETE | /api/v1/tasks/{taskId} | 200 | Cancel a queued task; repeated cancellation returns the same snapshot |

Submission example:

```json
{"capability":"analytics","payload":{"input":"demo"}}
```

- capability: required, 1-64 characters; starts with an ASCII letter/digit, followed by letters/digits, dot, underscore, or hyphen. Case-sensitive.
- payload: required string-to-string object with up to 16 entries; may be empty. Keys must be nonblank and at most 64 characters. Values must be non-null and at most 1024 characters. These are Java string-length limits.
- capability is a routing label, not a class name or executable command.

TaskResponse fields: UUID taskId, String capability, Map<String,String> payload, TaskStatus status, Instant submittedAt, Instant updatedAt. Timestamps use ISO-8601 UTC.

Errors use application/problem+json: 400 for invalid input/UUID/JSON, 404 for an unknown task, 503 when storage is full. Validation errors contain an errors array of field/message pairs. Rejected values are not included in the response.

Only QUEUED and CANCELLED are implemented. Tasks are not dispatched, executed, timed out, or aggregated. Cancellation preserves the record for lookup. Cancelled records still count toward capacity. State is process-local and lost at restart. Repeated POST requests create distinct tasks; idempotency keys are not implemented.

## Java Ports

Package: com.zaalima.orchestrator.integration.

| Interface | Production Owner | Input / Output | Required Behavior |
| --- | --- | --- | --- |
| AgentDirectory.findAvailable(String capability) | Ahamed | Capability -> List<Agent> | Registered healthy agents supporting that exact capability, or an empty list; never null. Return stable snapshots; exclude stale heartbeats. Discovery does not implement least-utilized-node balancing. |
| AgentClient.execute(Agent agent, Task task) | Ahamed | Agent + immutable task -> AgentExecutionResult | Blocking I/O suitable for Robin's virtual threads. Enforce transport timeouts/authentication. Return matching taskId/agentId and immutable output. Propagate I/O failure as IOException and interruption as InterruptedException. |
| TaskExecutionEngine.submit(Callable<T> work) | Robin | Work -> CompletableFuture<T> | Submit asynchronously, completing with result or exception. Reject unavailable capacity with RejectedExecutionException. Agree/test cancellation, interruption, and shutdown before integration. No executor is implemented here. |

AgentExecutionResult contains UUID taskId, String agentId, and Map<String,String> output, copied at construction. It represents success; failures use the client exception contract. Result validation against the dispatched task and aggregation remain HARI's future work.

Ahamed's existing Agent and AgentRegistry remain unchanged. AgentRegistry provides registration/ID lookup/heartbeat/removal signatures, without an implementation or capability discovery. Agree duplicate-registration identity, heartbeat expiry, endpoint metadata, REST/gRPC payloads, and security before wiring directory/client adapters.

## Redis Boundaries Still Pending

- CoordinationLock: agree key format, ownership token, TTL, renewal, and atomic owner-checked release with Navitha.
- TaskEventPublisher: agree event/task IDs, event version, payload, ordering, and reconnect/loss behavior. Pub/Sub must not be treated as durable delivery.
- No Redis/gRPC/execution-engine implementation is required to run today's API. No teammate port is called yet.

## Proposed Simulated-Agent MVP

The PDF mentions analytics/profile fetching but does not define a business payload schema. Proposed next test fixture: deterministic analytics and profiles agents with configurable delay/failure. Keep these in tests or an explicitly approved demo profile, then verify dispatch/cancellation/aggregation without claiming production integration or load-target compliance.

## Configuration and Limits

- SERVER_ADDRESS: defaults to 127.0.0.1 for this unauthenticated development API.
- SERVER_PORT: defaults to 8080.
- MAX_STORED_TASKS: defaults to 1000; minimum 1.
- Persistence, retention eviction, and public deployment security are pending.

package com.zaalima.orchestrator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.JsonNode;
import com.zaalima.orchestrator.agent.Agent;
import com.zaalima.orchestrator.api.dto.SubmitTaskRequest;
import com.zaalima.orchestrator.api.dto.TaskResponse;
import com.zaalima.orchestrator.api.dto.TaskResultResponse;
import com.zaalima.orchestrator.core.TaskStatus;
import com.zaalima.orchestrator.integration.AgentClient;
import com.zaalima.orchestrator.integration.AgentDirectory;
import com.zaalima.orchestrator.integration.AgentExecutionResult;
import com.zaalima.orchestrator.support.TestExecutionEngine;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "orchestrator.execution.enabled=true", "orchestrator.execution.timeout=2s"})
@Import(OrchestrationApiIntegrationTest.TestAdapters.class)
class OrchestrationApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private WaitingAgent waiting;

    private TaskResponse submit(String capability) {
        var response = rest.postForEntity("/api/v1/tasks",
                new SubmitTaskRequest(capability, Map.of("input", "demo")), TaskResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        return response.getBody();
    }

    private TaskResponse awaitTerminal(UUID id) {
        return await().atMost(Duration.ofSeconds(8)).until(
                () -> rest.getForObject("/api/v1/tasks/" + id, TaskResponse.class),
                task -> task != null && task.status().isTerminal());
    }

    @Test
    void httpSubmissionExecutesAndAggregatesResultsFromTwoSimulatedAgents() {
        TaskResponse task = submit("analytics");
        assertThat(awaitTerminal(task.taskId()).status()).isEqualTo(TaskStatus.SUCCEEDED);
        var response = rest.getForEntity("/api/v1/tasks/" + task.taskId() + "/result", TaskResultResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().results()).containsExactly(
                Map.entry("test-agent-a", Map.of("input", "demo", "agent", "test-agent-a")),
                Map.entry("test-agent-b", Map.of("input", "demo", "agent", "test-agent-b")));
        assertThat(response.getBody().failure()).isNull();
    }

    @Test
    void noAvailableAgentBecomesAFailedTask() {
        TaskResponse task = submit("missing");
        assertThat(awaitTerminal(task.taskId()).status()).isEqualTo(TaskStatus.FAILED);
        TaskResultResponse result = rest.getForObject("/api/v1/tasks/" + task.taskId() + "/result", TaskResultResponse.class);
        assertThat(result.failure().code()).isEqualTo("NO_AGENTS");
        assertThat(result.results()).isEmpty();
    }

    @Test
    void failedAgentReturnsSafeFailureDetails() {
        TaskResponse task = submit("failure");
        awaitTerminal(task.taskId());
        String response = rest.getForObject("/api/v1/tasks/" + task.taskId() + "/result", String.class);
        assertThat(response).contains("AGENT_FAILURE").doesNotContain("private-test-credential");
    }

    @Test
    void deadlineProducesAQueryableFailureResult() {
        TaskResponse task = submit("slow");
        assertThat(awaitTerminal(task.taskId()).status()).isEqualTo(TaskStatus.FAILED);
        TaskResultResponse result = rest.getForObject("/api/v1/tasks/" + task.taskId() + "/result", TaskResultResponse.class);
        assertThat(result.failure().code()).isEqualTo("TIMED_OUT");
    }

    @Test
    void runningCancellationIsIdempotentAndInterruptsTheTestAdapter() throws Exception {
        TaskResponse task = submit("wait");
        assertThat(waiting.started.await(3, TimeUnit.SECONDS)).isTrue();
        String path = "/api/v1/tasks/" + task.taskId();
        try {
            assertThat(rest.getForEntity(path + "/result", JsonNode.class).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);
            var cancellation = rest.exchange(path, HttpMethod.DELETE, HttpEntity.EMPTY, TaskResponse.class);
            assertThat(cancellation.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(cancellation.getBody().status()).isEqualTo(TaskStatus.CANCELLED);
            assertThat(rest.exchange(path, HttpMethod.DELETE, HttpEntity.EMPTY, TaskResponse.class).getBody())
                    .isEqualTo(cancellation.getBody());
            assertThat(waiting.interrupted.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(rest.getForObject(path + "/result", TaskResultResponse.class).results()).isEmpty();
        } finally {
            waiting.release.countDown();
        }
    }

    @Test
    void completedTaskCannotBeCancelledThroughHttp() {
        TaskResponse task = submit("analytics");
        awaitTerminal(task.taskId());
        var response = rest.exchange("/api/v1/tasks/" + task.taskId(), HttpMethod.DELETE, HttpEntity.EMPTY, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().path("status").asInt()).isEqualTo(409);
    }

    @Test
    void unknownResultReturns404() {
        assertThat(rest.getForEntity("/api/v1/tasks/" + UUID.randomUUID() + "/result", JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void independentTasksNeverShareResultData() {
        var ids = java.util.stream.IntStream.range(0, 8).mapToObj(index -> {
            var response = rest.postForEntity("/api/v1/tasks",
                    new SubmitTaskRequest("analytics", Map.of("input", "request-" + index)), TaskResponse.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
            return response.getBody().taskId();
        }).toList();
        for (int index = 0; index < ids.size(); index++) {
            UUID id = ids.get(index);
            assertThat(awaitTerminal(id).status()).isEqualTo(TaskStatus.SUCCEEDED);
            TaskResultResponse result = rest.getForObject("/api/v1/tasks/" + id + "/result", TaskResultResponse.class);
            for (Map<String, String> output : result.results().values()) {
                assertThat(output).containsEntry("input", "request-" + index);
            }
        }
    }

    static class WaitingAgent {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);
    }

    // These beans exist only in tests; no production registry, transport, or executor is supplied here.
    @TestConfiguration(proxyBeanMethods = false)
    static class TestAdapters {
        @Bean(destroyMethod = "close")
        TestExecutionEngine testExecutionEngine() {
            return new TestExecutionEngine();
        }

        @Bean
        WaitingAgent waitingAgent() {
            return new WaitingAgent();
        }

        @Bean
        AgentDirectory testDirectory() {
            return capability -> capability.equals("missing") ? List.of()
                    : List.of(agent("test-agent-a", capability), agent("test-agent-b", capability));
        }

        private Agent agent(String id, String capability) {
            return new Agent(id, "test-only", "test", Set.of(capability), Map.of(), Instant.EPOCH, Instant.EPOCH);
        }

        @Bean
        AgentClient testClient(WaitingAgent waiting) {
            return (agent, task) -> {
                if (task.capability().equals("failure")) {
                    throw new IOException("private-test-credential");
                }
                if (task.capability().equals("slow")) {
                    Thread.sleep(10_000);
                }
                if (task.capability().equals("wait")) {
                    waiting.started.countDown();
                    try {
                        waiting.release.await();
                    } catch (InterruptedException exception) {
                        waiting.interrupted.countDown();
                        throw exception;
                    }
                }
                return new AgentExecutionResult(task.taskId(), agent.id(),
                        Map.of("input", task.payload().get("input"), "agent", agent.id()));
            };
        }
    }
}

package com.zaalima.orchestrator.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaalima.orchestrator.config.TaskProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-15T09:00:00Z");

    private TaskService service(int capacity) {
        return new TaskService(new TaskProperties(capacity), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void submittedTaskIsQueuedAndCanBeRetrieved() {
        TaskService service = service(2);
        Task task = service.submit("analytics", Map.of("input", "demo"));

        assertThat(task.taskId()).isNotNull();
        assertThat(task.status()).isEqualTo(TaskStatus.QUEUED);
        assertThat(task.submittedAt()).isEqualTo(NOW);
        assertThat(task.updatedAt()).isEqualTo(NOW);
        assertThat(service.findById(task.taskId())).isEqualTo(task);
    }

    @Test
    void payloadCannotBeChangedAfterSubmission() {
        TaskService service = service(1);
        Map<String, String> payload = new HashMap<>(Map.of("input", "original"));
        Task task = service.submit("analytics", payload);
        payload.put("input", "changed");

        assertThat(task.payload()).containsEntry("input", "original");
        assertThatThrownBy(() -> task.payload().put("extra", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void cancellationIsIdempotentAndPreservesOriginalSubmission() {
        TaskService service = service(1);
        Task task = service.submit("analytics", Map.of());
        Task cancelled = service.cancel(task.taskId());

        assertThat(cancelled.status()).isEqualTo(TaskStatus.CANCELLED);
        assertThat(cancelled.submittedAt()).isEqualTo(task.submittedAt());
        assertThat(service.cancel(task.taskId())).isEqualTo(cancelled);
        assertThat(service.findById(task.taskId())).isEqualTo(cancelled);
        assertThat(task.status()).isEqualTo(TaskStatus.QUEUED);
    }

    @Test
    void unknownTaskCannotBeRetrievedOrCancelled() {
        TaskService service = service(1);
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.findById(unknown)).isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> service.cancel(unknown)).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void reachingCapacityDoesNotOverwriteExistingTask() {
        TaskService service = service(1);
        Task task = service.submit("analytics", Map.of());

        assertThatThrownBy(() -> service.submit("profiles", Map.of()))
                .isInstanceOf(TaskCapacityException.class);
        assertThat(service.findById(task.taskId())).isEqualTo(task);
    }

    @Test
    void cancelledTasksRemainStoredAndStillCountTowardsCapacity() {
        TaskService service = service(1);
        Task task = service.submit("analytics", Map.of());
        service.cancel(task.taskId());

        assertThatThrownBy(() -> service.submit("analytics", Map.of()))
                .isInstanceOf(TaskCapacityException.class);
    }

    @Test
    void failedConstructionDoesNotLeakStorageSlot() {
        TaskService service = service(1);

        assertThatThrownBy(() -> service.submit("analytics", null)).isInstanceOf(NullPointerException.class);
        assertThat(service.submit("analytics", Map.of()).status()).isEqualTo(TaskStatus.QUEUED);
    }

    @Test
    void concurrentSubmissionsCannotExceedStorageCapacity() throws Exception {
        TaskService service = service(10);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var attempts = IntStream.range(0, 60).mapToObj(index -> executor.submit(() -> {
                try {
                    return service.submit("analytics", Map.of("index", Integer.toString(index)));
                } catch (TaskCapacityException exception) {
                    return null;
                }
            })).toList();
            int accepted = 0;
            for (Future<Task> attempt : attempts) {
                Task task = attempt.get();
                if (task != null) {
                    accepted++;
                    assertThat(service.findById(task.taskId())).isEqualTo(task);
                }
            }
            assertThat(accepted).isEqualTo(10);
        }
    }
}

package com.zaalima.orchestrator.core;

import com.zaalima.orchestrator.config.TaskProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final ConcurrentMap<UUID, Task> tasks = new ConcurrentHashMap<>();
    private final Semaphore storageSlots;
    private final Clock clock;

    public TaskService(TaskProperties properties, Clock clock) {
        this.storageSlots = new Semaphore(properties.maxStoredTasks());
        this.clock = clock;
    }

    public Task submit(String capability, Map<String, String> payload) {
        if (!storageSlots.tryAcquire()) {
            throw new TaskCapacityException();
        }
        try {
            Instant now = clock.instant();
            Task task;
            do {
                task = new Task(UUID.randomUUID(), capability, payload, TaskStatus.QUEUED, now, now);
            } while (tasks.putIfAbsent(task.taskId(), task) != null);
            log.info("Task accepted taskId={} status={}", task.taskId(), task.status());
            return task;
        } catch (RuntimeException exception) {
            storageSlots.release();
            throw exception;
        }
    }

    public Task findById(UUID taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new TaskNotFoundException(taskId);
        }
        return task;
    }

    public Task cancel(UUID taskId) {
        Task task = tasks.compute(taskId, (id, existing) -> {
            if (existing == null) {
                throw new TaskNotFoundException(id);
            }
            return existing.cancel(clock.instant());
        });
        log.info("Task cancellation requested taskId={} status={}", taskId, task.status());
        return task;
    }
}

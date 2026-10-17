package com.zaalima.orchestrator.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Task(UUID taskId, String capability, Map<String, String> payload,
                   TaskStatus status, Instant submittedAt, Instant updatedAt) {

    public Task {
        payload = Map.copyOf(payload);
    }

    public Task cancel(Instant now) {
        if (status == TaskStatus.CANCELLED) {
            return this;
        }
        return new Task(taskId, capability, payload, TaskStatus.CANCELLED, submittedAt, now);
    }
}

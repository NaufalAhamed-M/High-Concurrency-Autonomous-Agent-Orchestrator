package com.zaalima.orchestrator.api.dto;

import com.zaalima.orchestrator.core.Task;
import com.zaalima.orchestrator.core.TaskStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TaskResponse(UUID taskId, String capability, Map<String, String> payload,
                           TaskStatus status, Instant submittedAt, Instant updatedAt) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.taskId(), task.capability(), task.payload(),
                task.status(), task.submittedAt(), task.updatedAt());
    }
}

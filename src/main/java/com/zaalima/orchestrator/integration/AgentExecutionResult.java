package com.zaalima.orchestrator.integration;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record AgentExecutionResult(UUID taskId, String agentId, Map<String, String> output) {

    public AgentExecutionResult {
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(agentId, "agentId");
        output = Map.copyOf(output);
    }
}

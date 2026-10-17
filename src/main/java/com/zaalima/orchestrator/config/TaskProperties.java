package com.zaalima.orchestrator.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("orchestrator.tasks")
public record TaskProperties(@Min(1) int maxStoredTasks) {
}

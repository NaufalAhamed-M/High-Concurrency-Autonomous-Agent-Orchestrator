package com.zaalima.orchestrator.controller;

import com.zaalima.orchestrator.api.dto.HealthResponse;
import java.time.Instant;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final BuildProperties buildProperties;

    public HealthController(BuildProperties buildProperties) {

        this.buildProperties = buildProperties;

    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse(
                "UP",
                "High-Concurrency Autonomous Agent Orchestrator",
                buildProperties.getVersion(),
                Instant.now()));
    }
}

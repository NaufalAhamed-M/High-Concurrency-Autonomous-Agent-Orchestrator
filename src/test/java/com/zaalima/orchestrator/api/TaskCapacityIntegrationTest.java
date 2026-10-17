package com.zaalima.orchestrator.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.zaalima.orchestrator.api.dto.SubmitTaskRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "orchestrator.tasks.max-stored-tasks=1")
class TaskCapacityIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void capacityLimitReturns503WithoutLosingAcceptedTask() {
        SubmitTaskRequest request = new SubmitTaskRequest("analytics", Map.of());
        var first = rest.postForEntity("/api/v1/tasks", request, JsonNode.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        var second = rest.postForEntity("/api/v1/tasks", request, JsonNode.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(second.getBody().path("status").asInt()).isEqualTo(503);
        var saved = rest.getForEntity(first.getHeaders().getLocation(), JsonNode.class);
        assertThat(saved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(saved.getBody()).isEqualTo(first.getBody());
    }
}

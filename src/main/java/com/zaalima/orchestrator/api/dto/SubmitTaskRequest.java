package com.zaalima.orchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record SubmitTaskRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[a-zA-Z0-9][a-zA-Z0-9._-]*") String capability,
        @NotNull @Size(max = 16)
        Map<@NotBlank @Size(max = 64) String, @NotNull @Size(max = 1024) String> payload) {
}

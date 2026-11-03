package com.zaalima.orchestrator.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import com.zaalima.orchestrator.config.AgentProperties;

@Validated
@ConfigurationProperties("orchestrator.agents")
public record AgentProperties(@DefaultValue("45s") Duration heartbeatTtl) {

    public AgentProperties {
        if (heartbeatTtl == null || heartbeatTtl.isZero() || heartbeatTtl.isNegative()) {
            throw new IllegalArgumentException("orchestrator.agents.heartbeat-ttl must be positive");
        }
    }
}

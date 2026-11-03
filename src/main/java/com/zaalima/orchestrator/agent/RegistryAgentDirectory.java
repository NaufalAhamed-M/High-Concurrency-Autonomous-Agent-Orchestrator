package com.zaalima.orchestrator.agent;

import com.zaalima.orchestrator.config.AgentProperties;
import com.zaalima.orchestrator.integration.AgentDirectory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import com.zaalima.orchestrator.config.AgentProperties;

@Component
public class RegistryAgentDirectory implements AgentDirectory {
    private static final Comparator<Agent> STABLE_ORDER =
            Comparator.comparing(Agent::name).thenComparing(Agent::id);

    private final AgentRegistry registry;
    private final AgentProperties properties;
    private final Clock clock;

    public RegistryAgentDirectory(AgentRegistry registry, AgentProperties properties, Clock clock) {
        this.registry = registry;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public List<Agent> findAvailable(String capability) {
        if (capability == null || capability.isBlank()) {
            return List.of();
        }
        Instant now = clock.instant();
        Duration ttl = properties.heartbeatTtl();
        return registry.findAll().stream()
                .filter(agent -> agent.supports(capability))
                .filter(Agent::isReachable)
                .filter(agent -> agent.isAvailable(now, ttl))
                .sorted(STABLE_ORDER)
                .toList();
    }
}

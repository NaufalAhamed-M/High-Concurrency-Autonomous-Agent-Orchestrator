package com.zaalima.orchestrator.agent;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Agent(String id, String name, String version, Set<String> capabilities,
                    Map<String, String> metadata, Instant registeredAt, Instant lastHeartbeat) {

    public static Agent create(String name, String version, Collection<String> capabilities, Map<String, String> metadata) {
        Instant now = Instant.now();
        return new Agent(UUID.randomUUID().toString(), name, version, Set.copyOf(capabilities),
                metadata == null ? Map.of() : Map.copyOf(metadata), now, now);
    }

    public Agent withHeartbeat(Instant t) {
        return new Agent(id, name, version, capabilities, metadata, registeredAt, t);
    }
}

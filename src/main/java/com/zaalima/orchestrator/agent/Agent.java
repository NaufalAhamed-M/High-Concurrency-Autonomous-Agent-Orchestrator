package com.zaalima.orchestrator.agent;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Agent(String id, String name, String version, Set<String> capabilities,
                    Map<String, String> metadata,
                    Instant registeredAt, Instant lastHeartbeat, URI endpoint) {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https", "grpc", "grpcs");

    public Agent {
        if (endpoint != null) {
            String scheme = endpoint.getScheme();
            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("endpoint scheme must be one of " + ALLOWED_SCHEMES);
            }
            if (endpoint.getHost() == null) {
                throw new IllegalArgumentException("endpoint must include a host");
            }
        }
    }

    public Agent(String id, String name, String version, Set<String> capabilities,
                 Map<String, String> metadata, Instant registeredAt, Instant lastHeartbeat) {
        this(id, name, version, capabilities, metadata, registeredAt, lastHeartbeat, null);
    }

    public static Agent create(String name, String version, Collection<String> capabilities,
                               Map<String, String> metadata) {
        return create(name, version, capabilities, metadata, null, Instant.now());
    }

    public static Agent create(String name, String version, Collection<String> capabilities,
                               Map<String, String> metadata, URI endpoint, Instant now) {
        return new Agent(UUID.randomUUID().toString(), name, version, Set.copyOf(capabilities),
                metadata == null ? Map.of() : Map.copyOf(metadata), now, now, endpoint);
    }

    public Agent withHeartbeat(Instant t) {
        return new Agent(id, name, version, capabilities, metadata, registeredAt, t, endpoint);
    }

    public boolean supports(String capability) {
        return capability != null && capabilities.contains(capability);
    }

    public boolean isReachable() {
        return endpoint != null;
    }

    public boolean isAvailable(Instant now, Duration ttl) {
        return !lastHeartbeat.plus(ttl).isBefore(now);
    }
}

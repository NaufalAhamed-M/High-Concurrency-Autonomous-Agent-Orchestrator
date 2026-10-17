package com.zaalima.orchestrator.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.zaalima.orchestrator.agent.Agent;
import com.zaalima.orchestrator.agent.InMemoryAgentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryAgentRegistryTest {
    InMemoryAgentRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryAgentRegistry();
    }

    private static Agent agent(String name) {
        return Agent.create(name, "1.0", List.of("profile.fetch"), Map.of("team", "growth"));
    }

    @Test
    void registerStoresAndReturnsCandidate() {
        Agent a = agent("alpha");
        assertSame(a, registry.registerIfAbsent(a));
        assertEquals(a, registry.findById(a.id()).orElseThrow());
        assertEquals(1, registry.size());
    }

    @Test
    void registeringSameNameReturnsOriginalAgent() {
        Agent first = registry.registerIfAbsent(agent("alpha"));
        Agent second = registry.registerIfAbsent(agent("alpha"));
        assertEquals(first.id(), second.id());
        assertEquals(1, registry.size());
    }

    @Test
    void differentNamesGetDifferentIds() {
        Agent a = registry.registerIfAbsent(agent("alpha"));
        Agent b = registry.registerIfAbsent(agent("beta"));
        assertNotEquals(a.id(), b.id());
        assertEquals(2, registry.size());
    }

    @Test
    void unknownIdIsEmpty() {
        assertTrue(registry.findById("nope").isEmpty());
        assertTrue(registry.heartbeat("nope").isEmpty());
    }

    @Test
    void heartbeatAdvancesLastHeartbeatOnly() throws Exception {
        Agent a = registry.registerIfAbsent(agent("alpha"));
        Thread.sleep(5);
        Agent beat = registry.heartbeat(a.id()).orElseThrow();
        assertTrue(beat.lastHeartbeat().isAfter(a.lastHeartbeat()));
        assertEquals(a.registeredAt(), beat.registeredAt());
        assertEquals(a.name(), beat.name());
        assertEquals(beat, registry.findById(a.id()).orElseThrow());
    }

    @Test
    void removeFreesTheNameForReRegistration() {
        Agent a = registry.registerIfAbsent(agent("alpha"));
        assertTrue(registry.remove(a.id()));
        assertTrue(registry.findById(a.id()).isEmpty());
        assertEquals(0, registry.size());

        Agent again = registry.registerIfAbsent(agent("alpha"));
        assertNotEquals(a.id(), again.id());
    }

    @Test
    void removeUnknownReturnsFalse() {
        assertFalse(registry.remove("nope"));
    }

    @Test
    void heartbeatCannotResurrectRemovedAgent() {
        Agent a = registry.registerIfAbsent(agent("alpha"));
        registry.remove(a.id());
        assertTrue(registry.heartbeat(a.id()).isEmpty());
        assertEquals(0, registry.size());
    }

    @Test
    void agentIsImmutableSnapshot() {
        Agent a = agent("alpha");
        assertThrows(UnsupportedOperationException.class, () -> a.capabilities().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> a.metadata().put("k", "v"));
    }

    @Test
    void thousandsOfConcurrentRegistrationsOfOneNameYieldOneAgent() throws Exception {
        List<Future<String>> results = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5_000; i++) {
                results.add(executor.submit(() -> registry.registerIfAbsent(agent("shared")).id()));
            }
        }
        Set<String> ids = new java.util.HashSet<>();
        for (Future<String> f : results) ids.add(f.get());
        assertEquals(1, ids.size());
        assertEquals(1, registry.size());
    }

    @Test
    void concurrentDistinctRegistrationsAllSucceed() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> fs = new ArrayList<>();
            for (int i = 0; i < 5_000; i++) {
                String name = "agent-" + i;
                fs.add(executor.submit(() -> registry.registerIfAbsent(agent(name))));
            }
            for (Future<?> f : fs) f.get();
        }
        assertEquals(5_000, registry.size());
    }

    @Test
    void concurrentRegisterAndRemoveChurnNeverBreaksTheRegistry() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> fs = new ArrayList<>();
            for (int t = 0; t < 16; t++) {
                fs.add(executor.submit(() -> {
                    for (int i = 0; i < 2_000; i++) {
                        Agent a = registry.registerIfAbsent(agent("churn"));
                        assertNotNull(a);                       // never null mid-removal
                        registry.heartbeat(a.id());
                        registry.remove(a.id());
                    }
                }));
            }
            for (Future<?> f : fs) f.get();                      // rethrows any failure
        }
        assertEquals(0, registry.size());
    }
}

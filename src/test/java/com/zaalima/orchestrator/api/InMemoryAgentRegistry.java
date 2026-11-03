package com.zaalima.orchestrator.api;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.zaalima.orchestrator.agent.Agent;
import com.zaalima.orchestrator.agent.AgentRegistry;
import org.springframework.stereotype.Component;


@Component
public class InMemoryAgentRegistry implements AgentRegistry {
    private final ConcurrentHashMap<String, Agent> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> idByName = new ConcurrentHashMap<>();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final Clock clock;

    public InMemoryAgentRegistry(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Agent registerIfAbsent(Agent candidate) {
        writeLock.lock();
        try {
            String existingId = idByName.get(candidate.name());
            if (existingId != null) {
                Agent existing = byId.get(existingId);
                if (existing != null) return existing;
            }
            byId.put(candidate.id(), candidate);
            idByName.put(candidate.name(), candidate.id());
            return candidate;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<Agent> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<Agent> heartbeat(String id) {
        return Optional.ofNullable(byId.computeIfPresent(id, (k, a) -> a.withHeartbeat(clock.instant())));
    }

    @Override
    public boolean remove(String id) {
        writeLock.lock();
        try {
            Agent removed = byId.remove(id);
            if (removed == null) return false;
            idByName.remove(removed.name(), id);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<Agent> findAll() {
        return List.copyOf(byId.values());
    }

    @Override
    public int size() {
        return byId.size();
    }
}

package com.zaalima.orchestrator.agent;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class InMemoryAgentRegistry implements AgentRegistry {
    private final ConcurrentHashMap<String, Agent> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> idByName = new ConcurrentHashMap<>();
    private final ReentrantLock writeLock = new ReentrantLock();

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
        return Optional.ofNullable(byId.computeIfPresent(id, (k, a) -> a.withHeartbeat(Instant.now())));
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
    public int size() {
        return byId.size();
    }
}

package com.zaalima.orchestrator.agent;

import java.util.List;
import java.util.Optional;

public interface AgentRegistry {

    Agent registerIfAbsent(Agent candidate);

    Optional<Agent> findById(String id);

    Optional<Agent> heartbeat(String id);

    boolean remove(String id);

    List<Agent> findAll();

    int size();
}

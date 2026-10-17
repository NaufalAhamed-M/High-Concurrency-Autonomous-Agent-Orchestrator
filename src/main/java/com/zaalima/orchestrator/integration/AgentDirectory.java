package com.zaalima.orchestrator.integration;

import com.zaalima.orchestrator.agent.Agent;
import java.util.List;

public interface AgentDirectory {

    List<Agent> findAvailable(String capability);
}

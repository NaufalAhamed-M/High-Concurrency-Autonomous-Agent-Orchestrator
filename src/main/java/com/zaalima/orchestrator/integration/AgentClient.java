package com.zaalima.orchestrator.integration;

import com.zaalima.orchestrator.agent.Agent;
import com.zaalima.orchestrator.core.Task;
import java.io.IOException;

public interface AgentClient {

    AgentExecutionResult execute(Agent agent, Task task) throws IOException, InterruptedException;
}

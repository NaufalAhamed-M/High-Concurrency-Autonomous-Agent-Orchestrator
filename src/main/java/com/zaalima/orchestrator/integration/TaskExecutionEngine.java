package com.zaalima.orchestrator.integration;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public interface TaskExecutionEngine {

    <T> CompletableFuture<T> submit(Callable<T> work);
}

package com.zaalima.orchestrator.core;

public class TaskCapacityException extends RuntimeException {

    public TaskCapacityException() {
        super("Task storage capacity reached. No additional tasks can be accepted.");
    }
}

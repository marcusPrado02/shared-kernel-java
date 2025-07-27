package com.marcusprado02.sharedkernel.orchestration.workflow;


public class WorkflowException extends RuntimeException {
    public WorkflowException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public WorkflowException(String msg) {
        super(msg);
    }
}


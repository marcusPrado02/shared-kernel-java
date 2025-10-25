package com.marcusprado02.sharedkernel.workflow;

import com.marcusprado02.sharedkernel.workflow.model.WorkflowTask;

public interface ActivityDispatcher {
    void dispatch(WorkflowTask task); // default: enfileira numa queue local ou publica num broker
}

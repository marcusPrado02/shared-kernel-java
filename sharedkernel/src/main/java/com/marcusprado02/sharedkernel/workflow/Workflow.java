package com.marcusprado02.sharedkernel.workflow;

public interface Workflow<I, O> {
    O run(WorkflowContext ctx, I input) throws Exception;
}


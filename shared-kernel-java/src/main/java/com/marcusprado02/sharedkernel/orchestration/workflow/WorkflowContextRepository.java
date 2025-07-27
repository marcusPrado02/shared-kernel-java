package com.marcusprado02.sharedkernel.orchestration.workflow;

import java.util.Optional;

/**
 * Port para persistir/restaurar WorkflowContext
 */
public interface WorkflowContextRepository<C extends WorkflowContext> {
    void save(C context);

    Optional<C> findById(String workflowId);
}



package com.marcusprado02.sharedkernel.workflow;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import com.marcusprado02.sharedkernel.workflow.model.WorkflowInstance;
import com.marcusprado02.sharedkernel.workflow.model.WorkflowTask;

public interface WorkflowStore {
    WorkflowInstance load(String id);
    void appendHistory(String workflowId, String type, Object payload);
    void update(WorkflowInstance<?> instance);
    void enqueueTask(WorkflowTask task);
    Optional<WorkflowTask> pollTask(String queue, java.time.OffsetDateTime now);
    boolean claimTask(String taskId); // lock otimista
    boolean seenIdempotency(String activity, String idemKey);

    // ===== extensões recomendadas =====
    default void recordIdempotency(String activity, String idemKey) {
        appendHistory("-", "IdempotencyRecorded", Map.of("activity", activity, "key", idemKey));
    }
    default void completeTask(String taskId, String outputJson) {
        appendHistory("-", "TaskCompleted", Map.of("taskId", taskId, "outputJson", outputJson));
    }
    default void failTask(String taskId, String reason) {
        appendHistory("-", "TaskFailed", Map.of("taskId", taskId, "reason", reason));
    }
    default void rescheduleTask(String taskId, OffsetDateTime when) {
        appendHistory("-", "TaskRescheduled", Map.of("taskId", taskId, "when", when.toString()));
    }
}


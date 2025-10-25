package com.marcusprado02.sharedkernel.workflow;


import java.time.OffsetDateTime;
import java.util.Map;

import com.marcusprado02.sharedkernel.workflow.model.WorkflowTask;

final class DefaultActivityContext implements ActivityContext {

  private final WorkflowTask task;

  DefaultActivityContext(WorkflowTask task) { this.task = task; }

  @Override public String workflowId()   { return task.workflowId(); }
  @Override public String activityId()   { return task.taskId(); }
  @Override public String activityName() { return task.activityName(); }

  @Override
  public Map<String, Object> headers() {
    // WorkflowTask não expõe headers; retorne vazio
    return Map.of();
  }

  @Override
  public int attempt() {
    // attempts é primitivo (int)
    return task.attempts();
  }

  @Override
  public OffsetDateTime taskCreatedAt() {
    // Use readyAt() se disponível; senão, agora
    return task.readyAt() != null ? task.readyAt() : OffsetDateTime.now();
  }
}

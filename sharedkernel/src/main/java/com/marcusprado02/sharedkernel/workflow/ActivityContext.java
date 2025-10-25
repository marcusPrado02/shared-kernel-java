package com.marcusprado02.sharedkernel.workflow;

import java.time.OffsetDateTime;
import java.util.Map;

public interface ActivityContext {
  String workflowId();
  String activityId();        // id da tarefa
  String activityName();      // nome lógico da activity
  Map<String, Object> headers();
  int attempt();              // se você controlar retentativas
  OffsetDateTime taskCreatedAt();
}

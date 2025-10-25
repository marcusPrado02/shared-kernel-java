package com.marcusprado02.sharedkernel.workflow;

import java.time.Duration;

public interface WorkflowContext {
    String workflowId();
    void schedule(String activityName, Object input, String taskQueue);
    void setTimer(String timerId, Duration delay);
    void awaitSignal(String signalName);
    void publishDomainEvent(Object evt); // via Outbox
}

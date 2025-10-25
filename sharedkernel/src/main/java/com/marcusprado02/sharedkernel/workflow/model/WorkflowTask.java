package com.marcusprado02.sharedkernel.workflow.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity @Table(name="wf_tasks",
        indexes = @Index(name="idx_task_queue_ready", columnList="queue, readyAt"))
public class WorkflowTask {
    @Id public String taskId; // ULID
    public String workflowId;
    public String queue;
    public String activityName;
    @Lob public String inputJson;
    public OffsetDateTime readyAt;
    public int attempts;
    public OffsetDateTime nextAttemptAt;
    public String idemKey; // idempotency

    public String taskId() { return taskId; }
    public String workflowId() { return workflowId; }
    public String queue() { return queue; }
    public String activityName() { return activityName; }
    public String inputJson() { return inputJson; }
    public OffsetDateTime readyAt() { return readyAt; }
    public int attempts() { return attempts; }
    public OffsetDateTime nextAttemptAt() { return nextAttemptAt; }
    public String idemKey() { return idemKey; }

}
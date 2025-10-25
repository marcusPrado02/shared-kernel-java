package com.marcusprado02.sharedkernel.workflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity @Table(name="wf_history",
        indexes = @Index(name="idx_wf_hist_wf", columnList="workflowId, seq"))
public class WorkflowHistory {
    @Id @GeneratedValue Long id;
    public String workflowId;
    public long seq; // sequência monotônica
    public String type; // Started, ActivityScheduled, ActivityCompleted...
    @Lob public String payloadJson;
    public java.time.OffsetDateTime at;
}

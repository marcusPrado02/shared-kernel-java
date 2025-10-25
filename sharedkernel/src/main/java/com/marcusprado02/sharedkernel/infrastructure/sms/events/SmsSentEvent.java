package com.marcusprado02.sharedkernel.infrastructure.sms.events;

import com.marcusprado02.sharedkernel.events.domain.DomainEvent;
import java.time.Instant;


public class SmsSentEvent implements DomainEvent {
    private final String smsId;
    private final String to;
    private final String status;
    // Campos adicionais opcionais
    private String errorCode;
    private String tenantId;
    private Object metadata;

    public SmsSentEvent(String smsId, String to, String status, String errorCode,  String tenantId, Object metadata) {
        this.smsId = smsId;
        this.to = to;
        this.status = status;
        this.errorCode = errorCode;
        this.tenantId = tenantId;
        this.metadata = metadata;
    }

    public String getSmsId() {
        return smsId;
    }

    public String getTo() {
        return to;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String eventName() {
        return "SMS.SENT";
    }

    @Override
    public String id() {
        return smsId;
    }

    @Override
    public String aggregateId() {
        return smsId;
    }

    @Override
    public String aggregateType() {
        return "SMS";
    }

    @Override
    public String correlationId() {
        return tenantId;
    }

    @Override
    public String source() {
        return to;
    }

    @Override
    public Object data() {
        return metadata;
    }

    @Override
    public Instant occurredOn() {
        return Instant.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
    

    

}

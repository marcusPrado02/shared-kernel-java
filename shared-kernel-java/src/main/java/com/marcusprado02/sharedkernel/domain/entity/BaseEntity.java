package com.marcusprado02.sharedkernel.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseEntity<ID> implements Serializable {

    private UUID identifier;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public abstract ID getId();

    public abstract void setId(Long id);

    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

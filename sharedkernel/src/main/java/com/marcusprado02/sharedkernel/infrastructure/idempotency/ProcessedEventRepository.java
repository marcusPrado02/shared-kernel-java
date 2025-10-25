package com.marcusprado02.sharedkernel.infrastructure.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByProjectionNameAndEventId(String projectionName, String eventId);
}

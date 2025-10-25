package com.marcusprado02.sharedkernel.infrastructure.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectionOffsetRepository extends JpaRepository<ProjectionOffset, String> {}

package com.marcusprado02.sharedkernel.domain.snapshot.model;

import java.time.Instant;
import java.util.Map;

public record SnapshotMetadata(
        String tenantId,
        String aggregateType,
        String aggregateId,
        long aggregateVersion,      // versão do estado no momento do snapshot
        long lastEventSequence,     // offset do último evento aplicado
        int schemaVersion,          // versão do schema do snapshot
        Instant createdAt,
        Map<String, String> tags    // livre: região, AZ, nó, motivo, estratégia, etc.
) {}

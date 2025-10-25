package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.time.Instant;

public record SecretVersion(
        String versionId, Instant createdAt, boolean enabled, Instant rotationDue
) {}

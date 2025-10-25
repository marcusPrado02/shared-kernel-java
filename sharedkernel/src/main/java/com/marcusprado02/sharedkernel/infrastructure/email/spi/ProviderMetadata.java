package com.marcusprado02.sharedkernel.infrastructure.email.spi;

import java.util.Set;

import com.marcusprado02.sharedkernel.infrastructure.email.api.Capabilities;

public record ProviderMetadata(
        String id, String displayName, String version,
        Set<String> regions, Capabilities capabilities
) {}

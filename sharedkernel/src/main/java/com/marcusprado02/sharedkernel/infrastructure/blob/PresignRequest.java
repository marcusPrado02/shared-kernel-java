package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.time.Duration;
import java.util.Map;

public record PresignRequest(
        BlobId id,
        Duration ttl,
        Map<String,String> responseHeaders, // ex.: Content-Disposition, Cache-Control
        boolean forUpload,
        String contentTypeConstraint,
        Long maxSizeBytes
){}

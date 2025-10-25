package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record StatResult(
        BlobId id,
        boolean exists,
        long contentLength,
        String contentType,
        String eTag,
        String versionId,
        Instant lastModified,
        Map<String,String> metadata,
        List<BlobTag> tags
){}


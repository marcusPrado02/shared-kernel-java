package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.net.URL;
import java.time.Instant;
import java.util.Map;

public record UploadResult(
        BlobId id,
        String eTag,
        String versionId,
        long size,
        String contentType,
        Map<String,String> metadata,
        Instant lastModified,
        URL directUrl             // url “não-presign” (ex.: via CDN/origin), se disponível
){}


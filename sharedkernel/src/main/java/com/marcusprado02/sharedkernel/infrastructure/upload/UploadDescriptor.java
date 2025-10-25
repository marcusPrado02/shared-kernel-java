package com.marcusprado02.sharedkernel.infrastructure.upload;

import java.time.Instant;
import java.util.Map;

public record UploadDescriptor(
        String uploadId,
        UploadStatus status,
        String storageKey,        // ex.: "uploads/2025/09/01/<id>/"
        String bucket,            // p/ S3/GCS ou null
        long receivedBytes,
        int receivedChunks,
        int totalChunks,          // -1 se indeterminado
        Instant createdAt,
        Map<String,String> metadata
) {}

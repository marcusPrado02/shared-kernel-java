package com.marcusprado02.sharedkernel.infrastructure.upload;

import java.util.Map;

public record UploadSpec(
        String filename,
        String contentType,
        long expectedSize,             // -1 se desconhecido
        String expectedSha256,         // opcional; se presente valida no final
        Map<String, String> metadata,  // ex.: tenantId, ownerId, domain, tags
        boolean directToCloud          // true = cliente envia direto a S3/GCS via URL assinada
) {}

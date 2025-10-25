package com.marcusprado02.sharedkernel.infrastructure.upload;

public record ChunkRef(String uploadId, int index, long size, String sha256) {}
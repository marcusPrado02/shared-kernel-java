package com.marcusprado02.sharedkernel.infrastructure.upload;

public record CompleteResult(String uploadId, String objectUri, String sha256, long size) {}

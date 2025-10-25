package com.marcusprado02.sharedkernel.infrastructure.blob;


public record BlobId(String bucket, String key) {
    public String path() { return bucket + "/" + key; }
}


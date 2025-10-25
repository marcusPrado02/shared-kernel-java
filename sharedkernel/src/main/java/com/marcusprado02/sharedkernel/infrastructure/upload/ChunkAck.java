package com.marcusprado02.sharedkernel.infrastructure.upload;

public record ChunkAck(String uploadId, int index, String etag, long receivedTotal) {

    public ChunkAck(String uploadId2, int index2, Object etag2, long l) {
        this(uploadId2, index2, (String) etag2, l);
    }}

package com.marcusprado02.sharedkernel.infrastructure.blob;

public record BlobRange(long start, Long end) {
    public static BlobRange of(long start, long end){ return new BlobRange(start, end); }
    public static BlobRange from(long start){ return new BlobRange(start, null); }
}

package com.marcusprado02.sharedkernel.infrastructure.blob;

public record ListRequest(String bucket, String prefix, String delimiter, int pageSize, String cursor) {
    public static ListRequest of(String bucket, String prefix){ return new ListRequest(bucket, prefix, "/", 1000, null); }
}

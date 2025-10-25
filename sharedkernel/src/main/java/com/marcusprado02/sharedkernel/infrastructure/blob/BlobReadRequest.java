package com.marcusprado02.sharedkernel.infrastructure.blob;

public record BlobReadRequest(
        BlobId id,
        BlobRange range,
        boolean asStream,              // se false, backend pode baixar inteiro para arquivo temporário
        boolean resolveVersion,        // pode aceitar versionId no key e resolver
        boolean includeMetadata
){
    public static BlobReadRequest of(BlobId id){ return new BlobReadRequest(id, null, true, true, true); }
    public BlobReadRequest withRange(BlobRange r){ return new BlobReadRequest(id, r, asStream, resolveVersion, includeMetadata); }
}
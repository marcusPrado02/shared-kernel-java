package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.io.InputStream;
import java.util.Map;

public record DownloadResult(
        BlobId id,
        InputStream stream,
        long contentLength,
        String contentType,
        String eTag,
        String versionId,
        Map<String,String> metadata
) implements AutoCloseable {
    @Override public void close() throws Exception { stream.close(); }
}


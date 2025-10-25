package com.marcusprado02.sharedkernel.infrastructure.upload.spi;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.upload.*;

public interface UploadStateStore {
    UploadDescriptor create(UploadSpec spec);
    UploadDescriptor get(String uploadId);
    void markStatus(String uploadId, UploadStatus status, String reason);
    void addChunkMeta(String uploadId, int index, long size, String sha256, String etagOrPartId);
    boolean chunkExists(String uploadId, int index);
    List<ChunkRef> listChunks(String uploadId);
    void incrementProgress(String uploadId, long bytes);
    void setFinalDigest(String uploadId, String sha256, long size);
}

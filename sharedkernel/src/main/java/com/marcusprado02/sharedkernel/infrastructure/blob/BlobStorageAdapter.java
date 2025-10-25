package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// Contrato principal
public interface BlobStorageAdapter {
    UploadResult putObject(BlobWriteRequest req);
    DownloadResult getObject(BlobReadRequest req);
    void getObjectToFile(BlobReadRequest req, Path target);           // streaming -> arquivo
    StatResult stat(BlobId id);
    boolean exists(BlobId id);
    void delete(BlobId id);
    void deletePrefix(String bucket, String prefix);
    void copy(BlobId source, BlobId target, boolean overwrite, Map<String,String> metadataReplace);
    void move(BlobId source, BlobId target, boolean overwrite);
    ListResult list(ListRequest req);

    // Multipart / compose
    String createMultipartUpload(BlobWriteRequest init);               // returns uploadId
    void uploadPart(BlobId id, String uploadId, int partNumber, InputStream data, long size, ContentHash md5);
    UploadResult completeMultipartUpload(BlobId id, String uploadId, Map<Integer,String> partNumberToEtag);
    void abortMultipartUpload(BlobId id, String uploadId);

    // Presigned
    PresignResult presign(PresignRequest req);

    // Assíncrono
    CompletableFuture<UploadResult> putObjectAsync(BlobWriteRequest req);
    CompletableFuture<DownloadResult> getObjectAsync(BlobReadRequest req);
    CompletableFuture<Void> getObjectToFileAsync(BlobReadRequest req, Path target);
    CompletableFuture<StatResult> statAsync(BlobId id);

    default String backendName(){ return getClass().getSimpleName(); }
}

package com.marcusprado02.sharedkernel.infrastructure.upload.adapter;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.marcusprado02.sharedkernel.infrastructure.upload.spi.ObjectStorage;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Component
class S3ObjectStorage implements ObjectStorage {
    private final S3Client s3;
    private final S3Presigner presigner;

    // Constructor injection so final fields are initialized by Spring
    S3ObjectStorage(S3Client s3, S3Presigner presigner) {
        this.s3 = s3;
        this.presigner = presigner;
    }

    @Override
    public String putChunk(String bucket, String storageKey, int partIndex, InputStream data, long size, Map<String, String> headers) {
        // Para S3 multipart real, crie um uploadId e registre parts.
        // Simplificação: use UploadPart (requer uploadId prévio) → retorne ETag.
        throw new UnsupportedOperationException("Use presign + client-side direct upload para S3 multipart");
    }

    @Override
    public URI assemble(String bucket, String storageKey, List<String> partEtags) {
        // CompleteMultipartUploadRequest com uploadId + lista de etags (grave uploadId no store)
        // Retorna s3://bucket/key
        return URI.create("s3://"+bucket+"/"+storageKey);
    }

    @Override
    public URI putObject(String bucket, String storageKey, InputStream data, long size, Map<String, String> headers) {
        var req = PutObjectRequest.builder()
                .bucket(bucket).key(storageKey).contentLength(size)
                .contentType(headers.getOrDefault("Content-Type","application/octet-stream")).build();
        s3.putObject(req, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(data, size));
        return URI.create("s3://"+bucket+"/"+storageKey);
    }

    @Override
    public Map<Integer, URI> presignUploadParts(String bucket, String storageKey, int start, int count, long partSize, Map<String, String> headers) {
        Map<Integer, URI> urls = new LinkedHashMap<>();
        for (int i=0; i<count; i++){
            int partNumber = start + i;
            var presigned = presigner.presignUploadPart(b -> b
                    .signatureDuration(java.time.Duration.ofMinutes(15))
                    .uploadPartRequest(r -> r.bucket(bucket).key(storageKey)
                            .partNumber(partNumber)
                            .uploadId(findOrInitUploadId(bucket, storageKey))));
            urls.put(partNumber, URI.create(presigned.url().toString()));
        }
        return urls;
    }

    @Override
    public void abortMultipart(String bucket, String storageKey) {
        // Chame AbortMultipartUpload com uploadId registrado
    }

    private String findOrInitUploadId(String bucket, String key){ /* buscar no store; criar se não existir */ return "uploadId"; }
}

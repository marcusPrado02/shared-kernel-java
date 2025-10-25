package com.marcusprado02.sharedkernel.infrastructure.blob.adapter.s3;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.blob.*;
import com.marcusprado02.sharedkernel.infrastructure.blob.Encryption;
import com.marcusprado02.sharedkernel.infrastructure.blob.adapter.*;

public class S3BlobStorageAdapter extends BaseBlobStorageAdapter {

    private final S3Client s3;
    private final S3Presigner presigner;

    public S3BlobStorageAdapter(Tracer tracer, MeterRegistry meter, Retry retry, CircuitBreaker cb,
                                Region region, AwsCredentialsProvider creds) {
        super(tracer, meter, retry, cb);
        this.s3 = S3Client.builder().region(region).credentialsProvider(creds).build();
        this.presigner = S3Presigner.builder().region(region).credentialsProvider(creds).build();
    }

    @Override
    protected UploadResult doPut(BlobWriteRequest req) {
        PutObjectRequest.Builder b = PutObjectRequest.builder()
                .bucket(req.id().bucket())
                .key(req.id().key())
                .contentType(req.contentType())
                .acl(toAcl(req.acl()))
                .metadata(req.metadata().values());

        if (req.tags() != null && !req.tags().values().isEmpty()) {
            String tagStr = toTaggingHeader(req.tags());
            b.tagging(tagStr);
        }

        switch (req.encryption().type()){
            case SSE_S3 -> b.serverSideEncryption(ServerSideEncryption.AES256);
            case SSE_KMS -> b.serverSideEncryption(ServerSideEncryption.AWS_KMS).ssekmsKeyId(req.encryption().kmsKeyId());
            case CSE -> {} // client-side: cifrar o stream antes (hook externo)
            case NONE -> {}
        }

        if (req.retention().legalHold()) {
            b.objectLockLegalHoldStatus(ObjectLockLegalHoldStatus.ON);
        }
        if (req.retention().retainUntil() != null) {
            b.objectLockMode(ObjectLockMode.COMPLIANCE).objectLockRetainUntilDate(req.retention().retainUntil());
        }

        RequestBody body = req.contentLength() >= 0
                ? RequestBody.fromInputStream(req.data(), req.contentLength())
                : RequestBody.fromInputStream(req.data(), 10 * 1024 * 1024); // fallback

        PutObjectResponse resp = s3.putObject(b.build(), body);
        return new UploadResult(
                req.id(),
                resp.eTag(),
                resp.versionId(),
                req.contentLength(),
                req.contentType(),
                req.metadata().values(),
                Instant.now(),
                null
        );
    }

    @Override
    protected DownloadResult doGet(BlobReadRequest req) {
        GetObjectRequest.Builder b = GetObjectRequest.builder()
                .bucket(req.id().bucket())
                .key(req.id().key());

        if (req.range() != null) {
            var r = req.range();
            b.range(r.end() == null ? "bytes=" + r.start() + "-" : "bytes=" + r.start() + "-" + r.end());
        }

        ResponseInputStream<GetObjectResponse> stream = s3.getObject(b.build());
        GetObjectResponse r = stream.response();

        return new DownloadResult(
                req.id(),
                stream,
                r.contentLength(),
                r.contentType(),
                r.eTag(),
                r.versionId(),
                r.metadata()
        );
    }

    @Override
    public StatResult stat(BlobId id) {
        HeadObjectResponse head;
        try {
            head = s3.headObject(HeadObjectRequest.builder().bucket(id.bucket()).key(id.key()).build());
        } catch (NoSuchKeyException e) {
            return new StatResult(id, false, 0, null, null, null, null, Map.of(), List.of());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return new StatResult(id, false, 0, null, null, null, null, Map.of(), List.of());
            }
            throw e;
        }

        return new StatResult(
                id, true, head.contentLength(), head.contentType(), head.eTag(), head.versionId(),
                head.lastModified(), head.metadata(), List.of()
        );
    }

    @Override public boolean exists(BlobId id) { return stat(id).exists(); }

    @Override
    public void delete(BlobId id) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(id.bucket()).key(id.key()).build());
    }

    @Override
    public void deletePrefix(String bucket, String prefix) {
        ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build();
        ListObjectsV2Response list;
        do {
            list = s3.listObjectsV2(req);
            if (!list.contents().isEmpty()) {
                List<ObjectIdentifier> ids = new ArrayList<>();
                for (S3Object o : list.contents()) {
                    ids.add(ObjectIdentifier.builder().key(o.key()).build());
                }
                s3.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(ids).build()).build());
            }
            req = req.toBuilder().continuationToken(list.nextContinuationToken()).build();
        } while (list.isTruncated());
    }

    @Override
    public void copy(BlobId source, BlobId target, boolean overwrite, Map<String,String> metadataReplace) {
        CopyObjectRequest.Builder b = CopyObjectRequest.builder()
                .sourceBucket(source.bucket()).sourceKey(source.key())
                .destinationBucket(target.bucket()).destinationKey(target.key());
        if (metadataReplace != null && !metadataReplace.isEmpty()) {
            b.metadataDirective(MetadataDirective.REPLACE).metadata(metadataReplace);
        }
        if (!overwrite) {
            // S3 não tem "if not exists" nativo; implementar otimismo via HEAD + condicional ETag se necessário
        }
        s3.copyObject(b.build());
    }

    @Override public void move(BlobId source, BlobId target, boolean overwrite) {
        copy(source, target, overwrite, null);
        delete(source);
    }

    @Override
    public ListResult list(ListRequest req) {
        ListObjectsV2Request b = ListObjectsV2Request.builder()
                .bucket(req.bucket()).prefix(req.prefix()).delimiter(req.delimiter())
                .maxKeys(req.pageSize()).continuationToken(req.cursor()).build();
        ListObjectsV2Response r = s3.listObjectsV2(b);
        List<String> keys = r.contents().stream().map(S3Object::key).toList();
        return new ListResult(keys, r.nextContinuationToken(), r.isTruncated());
    }

    @Override
    public String createMultipartUpload(BlobWriteRequest init) {
        CreateMultipartUploadRequest.Builder b = CreateMultipartUploadRequest.builder()
                .bucket(init.id().bucket())
                .key(init.id().key())
                .contentType(init.contentType())
                .metadata(init.metadata().values())
                .acl(toAcl(init.acl()));
        if (init.encryption().type() == Encryption.Type.SSE_S3) b.serverSideEncryption(ServerSideEncryption.AES256);
        if (init.encryption().type() == Encryption.Type.SSE_KMS) b.serverSideEncryption(ServerSideEncryption.AWS_KMS).ssekmsKeyId(init.encryption().kmsKeyId());
        return s3.createMultipartUpload(b.build()).uploadId();
    }

    @Override
    public void uploadPart(BlobId id, String uploadId, int partNumber, InputStream data, long size, ContentHash md5) {
        UploadPartRequest.Builder b = UploadPartRequest.builder()
                .bucket(id.bucket()).key(id.key())
                .uploadId(uploadId).partNumber(partNumber).contentLength(size);
        if (md5 != null && "MD5".equalsIgnoreCase(md5.algorithm())) b.contentMD5(md5.valueBase64());
        UploadPartResponse resp = s3.uploadPart(b.build(), RequestBody.fromInputStream(data, size));
        // eTag retornado no response headers; parte do mapa no complete
    }

    @Override
    public UploadResult completeMultipartUpload(BlobId id, String uploadId, Map<Integer, String> partNumberToEtag) {
        CompletedMultipartUpload cmu = CompletedMultipartUpload.builder()
                .parts(partNumberToEtag.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> CompletedPart.builder().partNumber(e.getKey()).eTag(e.getValue()).build())
                        .toList())
                .build();
        CompleteMultipartUploadResponse r = s3.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder().bucket(id.bucket()).key(id.key()).uploadId(uploadId).multipartUpload(cmu).build()
        );
        return new UploadResult(id, r.eTag(), r.versionId(), 0, null, Map.of(), Instant.now(), null);
    }

    @Override
    public void abortMultipartUpload(BlobId id, String uploadId) {
        s3.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(id.bucket()).key(id.key()).uploadId(uploadId).build());
    }

    @Override
    public PresignResult presign(PresignRequest req) {
        if (req.forUpload()) {
            PutObjectRequest.Builder put = PutObjectRequest.builder()
                    .bucket(req.id().bucket()).key(req.id().key());
            if (req.contentTypeConstraint() != null) put.contentType(req.contentTypeConstraint());
            if (req.responseHeaders() != null && req.responseHeaders().get("Content-Disposition") != null) {
                put.contentDisposition(req.responseHeaders().get("Content-Disposition"));
            }
            PresignedPutObjectRequest p = presigner.presignPutObject(b -> b
                    .signatureDuration(req.ttl())
                    .putObjectRequest(put.build()));
            var headers = p.signedHeaders().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));
            return new PresignResult(p.url(), Instant.now().plus(req.ttl()), headers);
        } else {
            GetObjectRequest.Builder get = GetObjectRequest.builder()
                    .bucket(req.id().bucket()).key(req.id().key());
            if (req.responseHeaders() != null) {
                if (req.responseHeaders().get("Response-Content-Disposition") != null)
                    get.responseContentDisposition(req.responseHeaders().get("Response-Content-Disposition"));
                if (req.responseHeaders().get("Response-Content-Type") != null)
                    get.responseContentType(req.responseHeaders().get("Response-Content-Type"));
            }
            PresignedGetObjectRequest p = presigner.presignGetObject(b -> b
                    .signatureDuration(req.ttl())
                    .getObjectRequest(get.build()));
            var headers = p.signedHeaders().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));
            return new PresignResult(p.url(), Instant.now().plus(req.ttl()), headers);
        }
    }

    private static ObjectCannedACL toAcl(CannedAcl a){
        return switch (a) {
            case PRIVATE -> ObjectCannedACL.PRIVATE;
            case PUBLIC_READ -> ObjectCannedACL.PUBLIC_READ;
            case AUTHENTICATED_READ -> ObjectCannedACL.AUTHENTICATED_READ;
            case BUCKET_OWNER_FULL_CONTROL -> ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL;
        };
    }

    private static String toTaggingHeader(Tags t){
        return t.values().stream().map(x -> x.key()+"="+x.value()).reduce((a,b)->a+"&"+b).orElse("");
    }
}


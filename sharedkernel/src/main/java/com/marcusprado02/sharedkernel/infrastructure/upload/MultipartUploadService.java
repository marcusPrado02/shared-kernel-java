package com.marcusprado02.sharedkernel.infrastructure.upload;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.upload.error.UploadException;
import com.marcusprado02.sharedkernel.infrastructure.upload.spi.*;

public final class MultipartUploadService {
    private final UploadStateStore store;
    private final ObjectStorage storage;
    private final VirusScanner av;
    private final Digest digest;
    private final UploadPolicy policy;

    public record UploadPolicy(
            long maxSizeBytes,
            int maxChunks,
            long minChunkSize,
            long maxChunkSize,
            Set<String> allowedContentTypes,
            boolean requireFinalDigest
    ) {}

    public MultipartUploadService(UploadStateStore store, ObjectStorage storage, VirusScanner av, Digest digest, UploadPolicy policy){
        this.store = store; this.storage = storage; this.av = av; this.digest = digest; this.policy = policy;
    }

    public InitResult init(UploadSpec spec){
        validateInit(spec);
        var d = store.create(spec);
        return new InitResult(d.uploadId(), URI.create("/uploads/"+d.uploadId()), Map.of(
                "bucket", d.bucket(), "storageKey", d.storageKey(), "status", d.status().name()
        ));
    }

    public ChunkAck uploadChunk(String uploadId, int index, long declaredSize, String declaredSha256, InputStream data){
        var d = store.get(uploadId);
        ensure(d.status() == UploadStatus.INIT || d.status() == UploadStatus.UPLOADING, "invalid_state");
        ensure(index >= 0 && index < policy.maxChunks(), "index_out_of_range");
        ensure(!store.chunkExists(uploadId, index), "chunk_already_uploaded");
        ensure(declaredSize >= policy.minChunkSize() || declaredSize == -1, "chunk_too_small");
        ensure(declaredSize <= policy.maxChunkSize(), "chunk_too_large");

        // AV + digest de chunk (streaming)
        var tee = new HashingInputStream(data, digest); // assume existente no seu projeto
        try {
            av.scan(tee.peek());
        } catch (Exception e) {
            throw new UploadException("virus_scan_failed", e);
        }

        String sha = (declaredSha256 != null) ? declaredSha256 : tee.sha256();
        long measuredSize = tee.bytesRead();

        if (declaredSize > 0) ensure(measuredSize == declaredSize, "size_mismatch");
        if (declaredSha256 != null) ensure(sha.equalsIgnoreCase(declaredSha256), "checksum_mismatch");

        var etag = storage.putChunk(d.bucket(), d.storageKey(), index, tee.rewind(), measuredSize, Map.of(
                "Content-Type", Optional.ofNullable(d.metadata().get("contentType")).orElse("application/octet-stream")
        ));
        store.addChunkMeta(uploadId, index, measuredSize, sha, etag);
        store.incrementProgress(uploadId, measuredSize);
        if (d.status() == UploadStatus.INIT) store.markStatus(uploadId, UploadStatus.UPLOADING, null);
        return new ChunkAck(uploadId, index, etag, d.receivedBytes()+measuredSize);
    }

    public CompleteResult complete(String uploadId, Integer totalChunks){
        var d = store.get(uploadId);
        ensure(d.status()==UploadStatus.UPLOADING || d.status()==UploadStatus.VERIFYING, "invalid_state_complete");

        var parts = store.listChunks(uploadId).stream()
                .sorted(Comparator.comparingInt(ChunkRef::index))
                .map(ChunkRef::sha256) // ou etag/partId conforme storage
                .toList();

        if (totalChunks != null) ensure(parts.size()==totalChunks, "chunks_missing");

        store.markStatus(uploadId, UploadStatus.VERIFYING, null);

        URI objUri = storage.assemble(d.bucket(), d.storageKey(), store.listChunks(uploadId).stream()
                .sorted(Comparator.comparingInt(ChunkRef::index))
                .map(ChunkRef::sha256 /* ou etag*/).toList());

        // Digest final: SHA256( concat(sha256(chunk_i)) )
        String finalSha = finalizeDigestFromChunks(store.listChunks(uploadId));
        if (policy.requireFinalDigest && d.metadata().getOrDefault("expectedSha256","").length() > 0) {
            ensure(finalSha.equalsIgnoreCase(d.metadata().get("expectedSha256")), "final_checksum_mismatch");
        }
        long size = store.listChunks(uploadId).stream().mapToLong(ChunkRef::size).sum();

        store.setFinalDigest(uploadId, finalSha, size);
        store.markStatus(uploadId, UploadStatus.ASSEMBLED, null);
        store.markStatus(uploadId, UploadStatus.AVAILABLE, null);

        return new CompleteResult(uploadId, objUri.toString(), finalSha, size);
    }

    public void abort(String uploadId, String reason){
        var d = store.get(uploadId);
        storage.abortMultipart(d.bucket(), d.storageKey());
        store.markStatus(uploadId, UploadStatus.ABORTED, reason);
    }

    public Map<Integer, URI> presign(String uploadId, int start, int count, long partSize){
        var d = store.get(uploadId);
        ensure(direct(d), "not_direct_upload");
        return storage.presignUploadParts(d.bucket(), d.storageKey(), start, count, partSize, Map.of(
                "Content-Type", d.metadata().getOrDefault("contentType", "application/octet-stream")
        ));
    }

    private boolean direct(UploadDescriptor d){ return Boolean.parseBoolean(d.metadata().getOrDefault("directToCloud","false")); }

    private void validateInit(UploadSpec s){
        if (s.expectedSize() > 0) ensure(s.expectedSize() <= policy.maxSizeBytes(), "file_too_large");
        if (!policy.allowedContentTypes().isEmpty()) {
            ensure(policy.allowedContentTypes().contains(s.contentType()), "content_type_not_allowed");
        }
    }

    private static void ensure(boolean cond, String code){ if (!cond) throw new UploadException(code); }

    private static String finalizeDigestFromChunks(List<ChunkRef> chunks){
        var joined = String.join("", chunks.stream()
                .sorted(Comparator.comparingInt(ChunkRef::index))
                .map(ChunkRef::sha256).toList());
        return sha256Hex(joined.getBytes(StandardCharsets.UTF_8));
    }

    // ---- util local para substituir HashUtils ----
    private static String sha256Hex(byte[] data){
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var hash = md.digest(data);
            var sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new UploadException("digest_error", e);
        }
    }
}
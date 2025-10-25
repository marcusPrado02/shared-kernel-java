package com.marcusprado02.sharedkernel.infrastructure.upload.store;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.marcusprado02.sharedkernel.infrastructure.upload.*;
import com.marcusprado02.sharedkernel.infrastructure.upload.model.*;
import com.marcusprado02.sharedkernel.infrastructure.upload.repository.*;
import com.marcusprado02.sharedkernel.infrastructure.upload.spi.*;

import jakarta.transaction.Transactional;

import java.util.List;

@Component
public class JpaRedisUploadStateStore implements UploadStateStore {

    private final UploadRepo uploadRepo;
    private final ChunkRepo chunkRepo;

    public JpaRedisUploadStateStore(UploadRepo uploadRepo, ChunkRepo chunkRepo) {
        this.uploadRepo = uploadRepo;
        this.chunkRepo = chunkRepo;
    }

    @Override
    @Transactional
    public UploadDescriptor create(UploadSpec spec) {
        var e = new UploadEntity();
        e.setUploadId(ulid());
        e.setStatus(UploadStatus.INIT);
        e.setBucket(bucketFor(spec));
        // Se usar Lombok fluent getter, troque getUploadId() por uploadId()
        e.setStorageKey("uploads/%s/%s".formatted(java.time.LocalDate.now(), e.uploadId()));
        e.setFilename(spec.filename());
        e.setContentType(spec.contentType());
        e.setExpectedSize(spec.expectedSize());
        e.setExpectedSha256(spec.expectedSha256());
        e.setMetadataJson(toJson(spec.metadata()));
        e.setCreatedAt(java.time.Instant.now());
        uploadRepo.save(e);
        return toDesc(e);
    }

    @Override
    public UploadDescriptor get(String uploadId) {
        return toDesc(uploadRepo.findById(uploadId).orElseThrow());
    }

    @Override
    @Transactional
    public void markStatus(String uploadId, UploadStatus status, String reason) {
        var e = uploadRepo.findById(uploadId).orElseThrow();
        e.setStatus(status);
        // se quiser, persista "reason" em um campo próprio / audit log
        uploadRepo.save(e);
    }

    @Override
    @Transactional
    public void addChunkMeta(String uploadId, int index, long size, String sha256, String etagOrPartId) {
        var c = new UploadChunkEntity();
        c.setUploadId(uploadId);
        c.setIdx(index);
        c.setSize(size);
        c.setSha256(sha256);
        c.setEtag(etagOrPartId);
        chunkRepo.save(c);

        var e = uploadRepo.findById(uploadId).orElseThrow();
        e.setReceivedBytes(e.receivedBytes() + size);
        e.setReceivedChunks(e.receivedChunks() + 1);
        uploadRepo.save(e);
    }

    @Override
    public boolean chunkExists(String uploadId, int index) {
        return chunkRepo.existsByUploadIdAndIdx(uploadId, index);
    }

    @Override
    public List<ChunkRef> listChunks(String uploadId) {
        return chunkRepo.findByUploadIdOrderByIdx(uploadId).stream()
                .map(c -> new ChunkRef(
                        c.uploadId(),
                        c.idx(),
                        c.size(),
                        c.sha256() // ou c.getEtag(), conforme o contrato do ChunkRef
                ))
                .toList();
    }

    @Override
    @Transactional
    public void incrementProgress(String uploadId, long bytes) {
        var e = uploadRepo.findById(uploadId).orElseThrow();
        e.setReceivedBytes(e.receivedBytes() + bytes);
        uploadRepo.save(e);
    }

    @Override
    @Transactional
    public void setFinalDigest(String uploadId, String sha256, long size) {
        var e = uploadRepo.findById(uploadId).orElseThrow();
        e.setFinalSha256(sha256);   // << antes era e.finalSha256 = ...
        e.setFinalSize(size);       // << antes era e.finalSize = ...
        uploadRepo.save(e);
    }

    
    private static UploadDescriptor toDesc(UploadEntity e) {
        // Troque getX() por x() se seu modelo usa Lombok @Accessors(fluent=true)
        return new UploadDescriptor(
                e.uploadId(),
                e.status(),
                e.storageKey(),
                e.bucket(),
                e.receivedBytes(),
                e.receivedChunks(),
                e.totalChunks(),
                e.createdAt(),
                (Map<String,String>) (Map<?,?>) fromJson(e.metadataJson())
        );
    }

    // --- helpers (existentes no seu projeto) ---
    private static String ulid() { /* ... */ throw new UnsupportedOperationException(); }
    private static String bucketFor(UploadSpec spec) { /* ... */ throw new UnsupportedOperationException(); }
    private static String toJson(Object o) { /* ... */ throw new UnsupportedOperationException(); }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJson(String s) { /* ... */ throw new UnsupportedOperationException(); }
}
package com.marcusprado02.sharedkernel.infrastructure.upload.model;

import java.time.Instant;

import com.marcusprado02.sharedkernel.infrastructure.upload.UploadStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity @Table(name="upload")
public class UploadEntity {
    @Id String uploadId;
    String bucket; 
    String storageKey; 
    String filename; 
    String contentType;
    long expectedSize; 
    String expectedSha256;
    long receivedBytes; 
    int receivedChunks; 
    int totalChunks;
    @Enumerated(EnumType.STRING) UploadStatus status;
    @Lob String metadataJson;
    Instant createdAt;
    String finalSha256; long finalSize;

    public String uploadId() {
        return uploadId;
    }

    public UploadStatus status() {
        return status;
    }

    public String bucket() {
        return bucket;
    }

    public String storageKey() {
        return storageKey;
    }

    public String filename() {
        return filename;
    }

    public String contentType() {
        return contentType;
    }

    public long expectedSize() {
        return expectedSize;
    }

    public String expectedSha256() {
        return expectedSha256;
    }

    public long receivedBytes() {
        return receivedBytes;
    }

    public int receivedChunks() {
        return receivedChunks;
    }

    public int totalChunks() {
        return totalChunks;
    }

    public String metadataJson() {
        return metadataJson;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String finalSha256() {
        return finalSha256;
    }

    public long finalSize() {
        return finalSize;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setExpectedSize(long expectedSize) {
        this.expectedSize = expectedSize;
    }

    public void setExpectedSha256(String expectedSha256) {
        this.expectedSha256 = expectedSha256;
    }

    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public void setReceivedChunks(int receivedChunks) {
        this.receivedChunks = receivedChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setFinalSha256(String finalSha256) {
        this.finalSha256 = finalSha256;
    }

    public void setFinalSize(long finalSize) {
        this.finalSize = finalSize;
    }
}

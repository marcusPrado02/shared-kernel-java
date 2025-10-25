package com.marcusprado02.sharedkernel.infrastructure.upload.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity @Table(name="upload_chunk", uniqueConstraints=@UniqueConstraint(columnNames={"uploadId","idx"}))
public class UploadChunkEntity {
    @Id @GeneratedValue
    private Long id;
    private String uploadId;
    private int idx;
    private long size;
    private String sha256;
    private String etag;

    public Long id() {
        return id;
    }

    public String uploadId() {
        return uploadId;
    }

    public int idx() {
        return idx;
    }

    public long size() {
        return size;
    }

    public String sha256() {
        return sha256;
    }

    public String etag() {
        return etag;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
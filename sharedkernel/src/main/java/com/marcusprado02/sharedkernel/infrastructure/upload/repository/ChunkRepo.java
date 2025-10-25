package com.marcusprado02.sharedkernel.infrastructure.upload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marcusprado02.sharedkernel.infrastructure.upload.model.UploadChunkEntity;

@Repository 
public interface ChunkRepo extends JpaRepository<UploadChunkEntity, Long> {
    boolean existsByUploadIdAndIdx(String uploadId, int idx);
    List<UploadChunkEntity> findByUploadIdOrderByIdx(String uploadId);
}

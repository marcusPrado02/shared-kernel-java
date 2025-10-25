package com.marcusprado02.sharedkernel.infrastructure.upload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marcusprado02.sharedkernel.infrastructure.upload.model.UploadEntity;

@Repository 
public interface UploadRepo extends JpaRepository<UploadEntity, String> {}

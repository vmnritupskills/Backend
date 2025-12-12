package com.example.login.repository;

import com.example.login.entity.ContentManagerBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentManagerBatchRepository extends JpaRepository<ContentManagerBatch, Long> {

    boolean existsByContentManagerIdAndBatchId(Long contentManagerId, Long batchId);
}

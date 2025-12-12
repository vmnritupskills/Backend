package com.example.login.service;

import com.example.login.dto.batch.BatchResp;
import com.example.login.dto.batch.CreateBatchReq;
import com.example.login.entity.Batch;
import com.example.login.entity.BatchYear;
import com.example.login.entity.User;
import com.example.login.repository.BatchRepository;
import com.example.login.repository.BatchYearRepository;
import com.example.login.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;
    private final BatchYearRepository batchYearRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------
    // CREATE BATCH
    // ----------------------------------------------------------
    @Transactional
    public BatchResp createBatch(CreateBatchReq req, Long createdBy) {

        // Validate batch year
        BatchYear year = batchYearRepository.findById(req.getBatchYearId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid batch year ID"));

        // Validate admin user
        User admin = userRepository.findById(createdBy)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        // Ensure same institution
        if (!admin.getInstitutionId().equals(year.getInstitutionId())) {
            throw new IllegalStateException("You cannot create a batch for another institution");
        }

        // Create batch
        Batch batch = Batch.builder()
                .batchYearId(req.getBatchYearId())
                .institutionId(admin.getInstitutionId())
                .name(req.getName())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();

        batchRepository.save(batch);

        return new BatchResp(batch.getId(), batch.getName(), batch.getBatchYearId());
    }
}

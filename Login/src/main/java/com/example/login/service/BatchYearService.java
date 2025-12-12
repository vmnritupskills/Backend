package com.example.login.service;

import com.example.login.dto.batchyear.BatchYearCreateReq;
import com.example.login.dto.batchyear.BatchYearResp;
import com.example.login.entity.BatchYear;
import com.example.login.repository.BatchYearRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BatchYearService {

    private final BatchYearRepository batchYearRepository;

    // ----------------------------------------------------------
    // CREATE BATCH YEAR
    // ----------------------------------------------------------
    @Transactional
    public BatchYearResp createBatchYear(BatchYearCreateReq req, Long createdBy) {

        BatchYear year = BatchYear.builder()
                .institutionId(req.getInstitutionId())     // Long ID
                .name(req.getName())
                .startYear(Instant.parse(req.getStartYear()))
                .endYear(Instant.parse(req.getEndYear()))
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();

        batchYearRepository.save(year);

        return new BatchYearResp(
                year.getId(),
                year.getName(),
                year.getInstitutionId()
        );
    }
}

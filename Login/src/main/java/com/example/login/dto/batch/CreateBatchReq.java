package com.example.login.dto.batch;

import lombok.Data;

import java.time.Instant;


@Data
public class CreateBatchReq {
    private Long batchYearId;
    private String name;
    private Instant startDate;
    private Instant endDate;
}


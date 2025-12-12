package com.example.login.dto.batchyear;

import lombok.Data;

@Data
public class BatchYearCreateReq {
    private Long institutionId;
    private String name;
    private String startYear;
    private String endYear;
}


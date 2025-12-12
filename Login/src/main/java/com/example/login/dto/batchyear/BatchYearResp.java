package com.example.login.dto.batchyear;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchYearResp {
    private Long id;
    private String name;
    private Long institutionId;
}


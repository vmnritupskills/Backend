package com.example.login.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchResp {
    private Long id;
    private String name;
    private Long batchYearId;
}

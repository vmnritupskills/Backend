package com.example.login.dto.content;

import lombok.Data;
import java.util.List;

@Data
public class AssignBatchReq {
    private List<Long> batchIds;
}

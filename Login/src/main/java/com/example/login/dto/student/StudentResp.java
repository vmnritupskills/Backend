package com.example.login.dto.student;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResp {
    private Long id;
    private String name;
    private String email;
    private Long batchId;
    private String rollNumber;
    private String branch;
    private String dateOfAdmission;
}

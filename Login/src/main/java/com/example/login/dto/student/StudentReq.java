package com.example.login.dto.student;

import lombok.Data;

@Data
public class StudentReq {
    private String name;
    private String email;
    private String rollNumber;
    private String branch;
    private String dateOfAdmission;
}

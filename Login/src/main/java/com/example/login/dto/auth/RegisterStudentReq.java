package com.example.login.dto.auth;

import lombok.Data;


@Data
public class RegisterStudentReq {
    private String name;
    private String email;
    private String password;
    private long institutionId;
}

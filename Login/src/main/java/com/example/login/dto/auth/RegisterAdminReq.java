package com.example.login.dto.auth;

import lombok.Data;

@Data
public class RegisterAdminReq {
    private String name;
    private String email;
    private String password;
}


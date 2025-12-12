package com.example.login.dto.auth;

import lombok.Data;

@Data
public class LoginReq {
    private String email;
    private String password;
    private Object deviceInfo;  // JSON allowed
}

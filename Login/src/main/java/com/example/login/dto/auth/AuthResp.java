package com.example.login.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResp {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String role;
}

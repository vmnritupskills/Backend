package com.example.login.dto.auth;

import io.jsonwebtoken.Claims;
import lombok.Data;

@Data
public class VerifyResp {
    private String subject;
    private Claims claims;
}

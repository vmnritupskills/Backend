package com.example.login.dto.content;

import lombok.Data;

@Data
public class CreateContentManagerReq {
    private String name;
    private String email;
    private String password;
}

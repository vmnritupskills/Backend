package com.example.login.dto.institution;

import lombok.Data;

@Data
public class CreateInstitutionReq {
    private String name;
    private String email;
    private String password;
}

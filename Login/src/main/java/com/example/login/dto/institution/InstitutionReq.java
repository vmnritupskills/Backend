package com.example.login.dto.institution;

import lombok.Data;

@Data
public class InstitutionReq {
    private String name;
    private String domain;
    private String description;
    private String settings; // JSON string
}

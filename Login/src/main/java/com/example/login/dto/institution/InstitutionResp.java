package com.example.login.dto.institution;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstitutionResp {
    private Long id;       // ✅ changed to Long
    private String name;
    private String email;
}

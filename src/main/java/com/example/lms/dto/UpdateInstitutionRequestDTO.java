package com.example.lms.dto;

public record UpdateInstitutionRequestDTO(
        String name,
        String address,
        String aisheCode,
        Boolean isActive
) {}

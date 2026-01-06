package com.example.lms.dto;

public record CreateInstitutionRequestDTO(
        String name,
        String email,
        String password,
        String address,
        String aisheCode

) {}


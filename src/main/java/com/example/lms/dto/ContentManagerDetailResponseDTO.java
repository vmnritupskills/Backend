package com.example.lms.dto;

public record ContentManagerDetailResponseDTO(
        Long id,
        String name,
        String email,
        String department,
        Long institutionId,
        Long userId,
        Boolean isActive
) {}


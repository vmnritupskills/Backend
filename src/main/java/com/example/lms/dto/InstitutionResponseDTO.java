package com.example.lms.dto;

import java.time.LocalDateTime;

public record InstitutionResponseDTO(
        Long id,
        String name,
        String address,
        String aisheCode,
        Long userId,
        Boolean isActive,
        LocalDateTime createdAt
) {}

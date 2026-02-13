package com.example.lms.dto;

import java.time.LocalDateTime;

public record ProfileResponseDTO(
        Long id,
        String name,
        String email,
        String role,
        Boolean isActive,
        LocalDateTime createdAt
) {}

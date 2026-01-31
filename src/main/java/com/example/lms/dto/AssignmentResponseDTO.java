package com.example.lms.dto;

import java.time.LocalDateTime;


public record AssignmentResponseDTO(
        Long id,
        String title,
        String questionFileUrl,
        LocalDateTime deadline
) {}


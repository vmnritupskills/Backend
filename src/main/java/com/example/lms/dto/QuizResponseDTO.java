package com.example.lms.dto;

public record QuizResponseDTO(
        Long id,
        String title,
        Boolean isActive,
        Integer passMarks
) {}

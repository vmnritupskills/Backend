package com.example.lms.dto;

public record QuizStatusDTO(
        Long id,
        String title,
        Boolean isActive,
        Integer passMarks
) {}

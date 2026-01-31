package com.example.lms.dto;

public record QuizResultDTO(
        Integer score,
        Boolean passed,
        Integer attemptNumber
) {}


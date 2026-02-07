package com.example.lms.dto;

public record UpdateCodingQuestionDTO(
        String title,
        String description,
        String exampleInput,
        String exampleOutput
) {}

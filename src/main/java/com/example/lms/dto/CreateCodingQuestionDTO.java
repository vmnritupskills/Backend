package com.example.lms.dto;

public record CreateCodingQuestionDTO(
        Long topicId,
        String title,
        String description,
        String exampleInput,
        String exampleOutput
) {}


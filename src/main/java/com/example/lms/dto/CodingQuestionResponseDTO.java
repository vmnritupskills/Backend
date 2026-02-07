package com.example.lms.dto;

public record CodingQuestionResponseDTO(
        Long id,
        String title,
        String description,
        String exampleInput,
        String exampleOutput,
        Long topicId
) {}

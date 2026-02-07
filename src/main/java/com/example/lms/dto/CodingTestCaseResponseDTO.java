package com.example.lms.dto;

public record CodingTestCaseResponseDTO(
        Long id,
        String input,
        String expectedOutput,
        Boolean hidden,
        Long questionId
) {}

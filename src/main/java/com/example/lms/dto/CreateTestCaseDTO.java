package com.example.lms.dto;

public record CreateTestCaseDTO(
        String input,
        String expectedOutput,
        Boolean hidden
) {}


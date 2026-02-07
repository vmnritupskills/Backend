package com.example.lms.dto;

public record UpdateTestCaseDTO(
        String input,
        String expectedOutput,
        Boolean hidden
) {}

package com.example.lms.dto;

public record SubmissionResultDTO(
        Integer passedTestCases,
        Integer totalTestCases,
        Boolean completed
) {}


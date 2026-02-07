package com.example.lms.dto;

import java.time.LocalDateTime;

public record CMSubmissionViewDTO(
        Long studentId,
        String studentName,
        Integer passedTestCases,
        Integer totalTestCases,
        Boolean completed,
        LocalDateTime submittedAt
) {}


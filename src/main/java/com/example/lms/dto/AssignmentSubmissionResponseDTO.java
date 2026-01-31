package com.example.lms.dto;

import java.time.LocalDateTime;

public record AssignmentSubmissionResponseDTO(
        Long id,
        Long studentId,
        String submissionFileUrl,
        LocalDateTime submittedAt,
        Integer marks
) {}


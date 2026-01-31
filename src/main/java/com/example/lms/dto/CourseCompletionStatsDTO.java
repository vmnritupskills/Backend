package com.example.lms.dto;

public record CourseCompletionStatsDTO(
        Long courseId,
        double averageCompletionPercentage,
        int totalStudents
) {}

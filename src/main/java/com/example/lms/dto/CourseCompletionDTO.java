package com.example.lms.dto;

public record CourseCompletionDTO(
        Long studentId,
        Long courseId,
        String courseName,
        Long totalTopics,
        Long totalSubtopics,
        Integer completionPercentage
) {}

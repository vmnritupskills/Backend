package com.example.lms.dto;
// CourseTopicResponseDTO

public record CourseTopicResponseDTO(
        Long id,
        String title,
        Integer durationMinutes,
        Long courseId
) {}


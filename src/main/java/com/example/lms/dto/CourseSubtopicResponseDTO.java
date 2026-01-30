package com.example.lms.dto;

// CourseSubtopicResponseDTO
public record CourseSubtopicResponseDTO(
        Long id,
        String title,
        ContentType contentType,
        String contentUrl,
        String textContent,
        Integer durationMinutes,
        Long topicId
) {}


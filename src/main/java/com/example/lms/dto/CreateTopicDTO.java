package com.example.lms.dto;

public record CreateTopicDTO(
        Long courseId,
        String title,
        Integer durationMinutes
) {}


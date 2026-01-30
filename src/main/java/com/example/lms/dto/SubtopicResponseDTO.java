package com.example.lms.dto;

public record SubtopicResponseDTO(
        Long subtopicId,
        String title,
        ContentType contentType,
        String contentUrl,
        String textContent,
        Integer durationMinutes
) {}

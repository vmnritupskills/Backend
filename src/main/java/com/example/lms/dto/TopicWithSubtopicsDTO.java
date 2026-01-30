package com.example.lms.dto;

import java.util.List;

public record TopicWithSubtopicsDTO(
        Long topicId,
        String title,
        Integer durationMinutes,
        List<SubtopicResponseDTO> subtopics
) {}

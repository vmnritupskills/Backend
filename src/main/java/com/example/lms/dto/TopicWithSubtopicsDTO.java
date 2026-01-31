package com.example.lms.dto;

import java.util.List;

public record TopicWithSubtopicsDTO(
        Long id,
        String title,
        Integer durationMinutes,
        List<SubtopicResponseDTO> subtopics,
        List<QuizStatusDTO> quizzes
) {}

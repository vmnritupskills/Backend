package com.example.lms.dto;

import java.util.List;

public record CreateQuizDTO(
        Long topicId,
        String title,
        List<CreateQuizQuestionDTO> questions
) {}


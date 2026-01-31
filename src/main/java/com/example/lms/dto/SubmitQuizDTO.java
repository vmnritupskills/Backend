package com.example.lms.dto;

import java.util.Map;

public record SubmitQuizDTO(
        Long quizId,
        Long studentId,
        Integer score
) {}




package com.example.lms.dto;

public record CreateQuizQuestionDTO(
        String question,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer
) {}


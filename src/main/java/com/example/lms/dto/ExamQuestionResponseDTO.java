package com.example.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExamQuestionResponseDTO {

    private Long id;
    private String questionType;
    private String question;
    private List<String> options;
    private String correctAnswer; // Optional (can remove for students)
    private String difficulty;
    private String topic;
}

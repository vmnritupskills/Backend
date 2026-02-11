package com.example.lms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuestionDTO {

    @JsonProperty("question_type")
    private String questionType;

    private String question;

    private List<String> options;

    @JsonProperty("correct_answer")
    private String correctAnswer;

    private String difficulty;
    private String topic;
}

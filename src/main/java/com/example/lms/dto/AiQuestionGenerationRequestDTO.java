package com.example.lms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionGenerationRequestDTO {

    @JsonProperty("content")
    private String content;

    @JsonProperty("question_types")
    private List<String> questionTypes;

    @JsonProperty("num_questions")
    private Integer numQuestions;

    @JsonProperty("difficulty_distribution")
    private Map<String, Double> difficultyDistribution;
}

package com.example.lms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuestionGenerationResponseDTO {

    private Boolean success;
    private List<AiQuestionDTO> questions;
    private List<String> errors;



}

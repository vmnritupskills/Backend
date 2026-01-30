package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTopicDTO(

        @NotBlank(message = "Topic title is required")
        String title,

        @NotNull(message = "Duration (minutes) is required")
        Integer durationMinutes

) {}

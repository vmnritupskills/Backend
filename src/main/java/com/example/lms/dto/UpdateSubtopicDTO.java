package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSubtopicDTO(

        @NotBlank
        String title,

        @NotNull
        ContentType contentType,

        String textContent,

        @NotNull
        Integer durationMinutes
) {}

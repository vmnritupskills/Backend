package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAssignmentDTO(

        @NotNull
        Long topicId,

        @NotBlank
        String title,

        @NotNull
        LocalDateTime deadline
) {}

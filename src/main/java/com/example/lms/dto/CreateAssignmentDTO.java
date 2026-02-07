package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat;

public record CreateAssignmentDTO(

        @NotNull
        Long topicId,

        @NotBlank
        String title,

        @NotNull
        @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
        LocalDateTime deadline
) {}



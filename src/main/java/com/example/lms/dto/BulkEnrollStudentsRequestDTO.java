package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkEnrollStudentsRequestDTO(

        @NotNull(message = "institutionId is required")
        Long institutionId,

        @NotNull(message = "courseCode is required")
        Long courseId,

        @NotEmpty(message = "studentIds list cannot be empty")
        List<@NotBlank(message = "studentId cannot be blank") String> studentIds
) {}

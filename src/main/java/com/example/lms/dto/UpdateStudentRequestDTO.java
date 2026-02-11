package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStudentRequestDTO(

        @NotNull(message = "institutionId is required")
        Long institutionId,

        @NotBlank(message = "Register number is required")
        String regNo,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        String email,

        Integer graduationYear,

        @NotBlank(message = "Department is required")
        String department,

        String password   // optional for update
) {}

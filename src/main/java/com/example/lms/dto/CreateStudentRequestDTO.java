package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStudentRequestDTO(

        @NotNull(message = "institutionId is required")
        Long institutionId,

        @NotBlank(message = "Register number is required")
        String regNo,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotNull(message = "Graduation year is required")
        Integer graduationYear,

        @NotBlank(message = "Department is required")
        String department
) {}

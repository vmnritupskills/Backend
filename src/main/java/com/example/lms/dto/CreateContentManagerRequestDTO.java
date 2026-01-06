package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateContentManagerRequestDTO(

        @NotNull(message = "institutionUserId is required")
        Long institutionId,

        @NotBlank(message = "name should not be empty")
        String name,

        @NotBlank(message = "email should not be empty")
        String email,

        @NotBlank(message = "password should not be empty")
        String password,

        @NotBlank(message = "department should not be empty")
        String department
) {}

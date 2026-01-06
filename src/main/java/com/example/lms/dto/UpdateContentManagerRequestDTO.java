package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateContentManagerRequestDTO(
        @NotBlank(message = "name should not be null")
        String name,
        @NotBlank(message = "department should not be null")
        String department,

        @NotNull(message = "isActive should not be null")
        Boolean isActive
) {}

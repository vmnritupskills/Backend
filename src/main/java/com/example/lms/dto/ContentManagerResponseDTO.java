package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;

public record ContentManagerResponseDTO(

        @NotBlank(message = "id should not be null")
        Long id,
        @NotBlank(message = "name should not be null")
        String name,
        @NotBlank(message = "email should not be null")
        String email,
        @NotBlank(message = "department should not be null")
        String department,
        @NotBlank(message = "user_Id should not be null")
        Long userId,
        @NotBlank(message = "isActive should not be null")
        Boolean isActive
) {}

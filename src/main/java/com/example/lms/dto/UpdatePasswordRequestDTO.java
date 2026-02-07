package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequestDTO(

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        String newPassword
) {}

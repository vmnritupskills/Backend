package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileDTO(
        @NotBlank String name
) {}

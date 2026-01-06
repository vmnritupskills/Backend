package com.example.lms.dto;

import jakarta.validation.constraints.NotNull;

public record AssignCoursesToContentManagerDTO(

        @NotNull(message = "courseId is required")
        Long courseId

) {}

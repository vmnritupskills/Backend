package com.example.lms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateAssignedCoursesDTO(

        @NotEmpty(message = "courseIds must not be empty")
        List<Long> courseIds
) {}

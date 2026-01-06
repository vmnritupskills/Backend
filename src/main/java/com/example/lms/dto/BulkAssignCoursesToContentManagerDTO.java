package com.example.lms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkAssignCoursesToContentManagerDTO(

        @NotNull(message = "institutionId is required")
        Long institutionId,

        @NotEmpty(message = "courseIds list cannot be empty")
        List<@NotNull(message = "courseId cannot be null") Long> courseIds

) {

}

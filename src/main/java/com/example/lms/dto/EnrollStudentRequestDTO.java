package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EnrollStudentRequestDTO(

        @NotNull(message = "institutionId is required")
        Long institutionId,

        @NotBlank(message = "studentId (regNo) is required")
        String studentId,

        @NotNull(message = "courseCode is required")
        Long courseId
) {

}

package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record CreateCourseRequestDTO(

        @NotNull(message = "institutionId is required")
        Long institutionId,

        @NotBlank(message = "course name is required")
        String name,

        @NotBlank(message = "course code is required")
        String courseCode,

        @NotBlank(message = "duration is required")
        String duration,

        @NotNull(message = "semester is required")
        Integer semester,

        MultipartFile syllabus
) {}

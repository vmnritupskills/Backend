package com.example.lms.dto;

import java.util.List;

public record StudentCourseResponseDTO(
        Long studentId,
        List<CourseDTO> courses
) {}

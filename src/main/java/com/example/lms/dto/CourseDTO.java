package com.example.lms.dto;

public record CourseDTO(
        Long id,
        String name,
        String courseCode,
        String duration,
        Integer semester
) {}

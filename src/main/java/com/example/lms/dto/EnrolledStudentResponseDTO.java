package com.example.lms.dto;

public record EnrolledStudentResponseDTO(
        Long studentId,
        String name,
        String email,
        String courseTitle
) {}

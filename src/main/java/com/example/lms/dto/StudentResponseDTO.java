package com.example.lms.dto;

public record StudentResponseDTO(

        Long id,
        String regNo,
        String name,
        String email,
        Integer graduationYear,
        String department,
        Long institutionId

) {}

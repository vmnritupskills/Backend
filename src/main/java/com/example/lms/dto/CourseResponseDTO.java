package com.example.lms.dto;

public record CourseResponseDTO(
        Long id,
        String name,
        String courseCode,
        String duration,
        Integer semester,
        String syllabusUrl,
        Long institutionId,
        String institutionName
) {

}

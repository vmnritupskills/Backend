package com.example.lms.dto;

import java.util.List;

public record CourseContentResponseDTO(
        Long courseId,
        List<TopicWithSubtopicsDTO> topics
) {}

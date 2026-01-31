package com.example.lms.dto;

import java.util.List;

public record CmBootstrapResponseDTO(
        Long cmId,
        String cmName,
        List<CourseSummaryDTO> courses
) {}


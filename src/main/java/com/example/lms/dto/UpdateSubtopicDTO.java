package com.example.lms.dto;

import com.example.lms.entity.CourseSubtopic;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubtopicDTO {

    @NotNull
    private String title;

    @NotNull
    private ContentType contentType;

    private String textContent;

    @NotNull
    private Integer durationMinutes;
}

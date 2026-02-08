package com.example.lms.dto;

import com.example.lms.entity.CourseSubtopic;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubtopicDTO {


    private String title;


    private ContentType contentType;

    private String textContent;


    private Integer durationMinutes;
}

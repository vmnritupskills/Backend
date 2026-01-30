package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSubtopicDTO {

    @NotNull
    private Long topicId;

    @NotBlank
    private String title;

    @NotNull
    private ContentType contentType;

    private String textContent;

    @NotNull
    private Integer durationMinutes;
}

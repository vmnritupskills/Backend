package com.example.lms.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExamRequest {

    private String title;

    private LocalDate startDate;
    private LocalTime startTime;

    private Integer durationMinutes;
    private Long courseId;

    // AI part
    private String content;
    private List<String> questionTypes;
    private Integer numQuestions;
    private MultipartFile document;

}

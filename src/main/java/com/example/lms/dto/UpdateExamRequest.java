package com.example.lms.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateExamRequest {

    private LocalDate startDate;
    private LocalTime startTime;
    private Integer durationMinutes;
    private ExamStatus status;
}

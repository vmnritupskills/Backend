package com.example.lms.entity;

import com.example.lms.dto.ExamStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "exams")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate startDate;
    private LocalTime startTime;
    private Integer durationMinutes;

    private Long courseId;

    @Enumerated(EnumType.STRING)
    private ExamStatus status; // DRAFT, PUBLISHED, CLOSED
}


package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_questions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examId;

    private String questionType; // mcq, true_false, coding

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String optionsJson; // JSON string

    private String correctAnswer;

    private String difficulty;
    private String topic;
}


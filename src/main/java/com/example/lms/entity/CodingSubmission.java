package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coding_submissions")
@Builder
@Getter
@Setter
@NoArgsConstructor          // ✅ REQUIRED
@AllArgsConstructor
public class CodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CodingQuestion codingQuestion;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(columnDefinition = "TEXT")
    private String sourceCode;



    private Integer passedTestCases;

    private Integer totalTestCases;

    private Boolean completed; // passed all test cases?

    private LocalDateTime submittedAt;


}


package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coding_test_cases")
@Builder
@Getter
@Setter
@NoArgsConstructor          // ✅ REQUIRED
@AllArgsConstructor
public class CodingTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    private Boolean hidden; // true = hidden, false = visible

    @ManyToOne
    @JoinColumn(name = "coding_question_id")
    private CodingQuestion codingQuestion;
}


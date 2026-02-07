package com.example.lms.entity;

import com.example.lms.entity.CourseTopic;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coding_questions")
@Builder
@Getter
@Setter
@NoArgsConstructor          // ✅ REQUIRED
@AllArgsConstructor
public class CodingQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String exampleInput;

    @Column(columnDefinition = "TEXT")
    private String exampleOutput;

    private Integer totalTestCases;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private CourseTopic topic;
}

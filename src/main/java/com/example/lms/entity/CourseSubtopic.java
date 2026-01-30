package com.example.lms.entity;

import com.example.lms.dto.ContentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_subtopics")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CourseSubtopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private String contentUrl;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    @JsonBackReference   // ✅ IMPORTANT
    private CourseTopic topic;
}

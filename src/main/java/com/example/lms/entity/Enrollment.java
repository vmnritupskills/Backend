package com.example.lms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "course_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= RELATIONS ================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /* ================= FIELDS ================= */

    @Builder.Default
    @Column(name = "is_enrolled", nullable = false)
    private Boolean isEnrolled = true;

    @Column(name = "enrolled_date", updatable = false)
    private LocalDateTime enrolledDate;

    @PrePersist
    protected void onCreate() {
        this.enrolledDate = LocalDateTime.now();
    }
}

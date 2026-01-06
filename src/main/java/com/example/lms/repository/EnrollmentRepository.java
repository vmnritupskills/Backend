package com.example.lms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.entity.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourse_Id(
            String studentId,
            Long courseId
    );

    Optional<Enrollment> findByStudentIdAndCourse_Id(
            String studentId,
            Long courseId
    );
}


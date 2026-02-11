package com.example.lms.repository;

import com.example.lms.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseIdIn(List<Long> courseIds);
}


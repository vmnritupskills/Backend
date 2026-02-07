package com.example.lms.repository;

import com.example.lms.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingSubmissionRepository
        extends JpaRepository<CodingSubmission, Long> {

    List<CodingSubmission> findByCodingQuestionId(Long questionId);
    boolean existsByCodingQuestionIdAndStudentId(Long qId, Long studentId);
}


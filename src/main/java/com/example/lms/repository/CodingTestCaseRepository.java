package com.example.lms.repository;

import com.example.lms.entity.CodingTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingTestCaseRepository
        extends JpaRepository<CodingTestCase, Long> {

    List<CodingTestCase> findByCodingQuestionId(Long questionId);
    List<CodingTestCase> findByCodingQuestionIdAndHiddenFalse(Long questionId);
}

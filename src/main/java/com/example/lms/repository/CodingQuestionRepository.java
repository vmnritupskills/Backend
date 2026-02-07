package com.example.lms.repository;

import com.example.lms.entity.CodingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingQuestionRepository
        extends JpaRepository<CodingQuestion, Long> {
}


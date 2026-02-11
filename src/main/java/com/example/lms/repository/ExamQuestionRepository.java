package com.example.lms.repository;

import com.example.lms.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamId(Long examId);
    void deleteByExamId(Long examId);

}


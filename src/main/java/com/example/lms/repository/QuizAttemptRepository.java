package com.example.lms.repository;

import com.example.lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository
        extends JpaRepository<QuizAttempt, Long> {

    long countByQuizIdAndStudentId(Long quizId, Long studentId);

    Optional<QuizAttempt> findTopByQuizIdAndStudentIdOrderByAttemptNumberDesc(
            Long quizId, Long studentId
    );

    List<QuizAttempt> findByQuizId(Long quizId);
    boolean existsByQuizIdAndStudentIdAndPassedTrue(Long quizId, Long studentId);
}

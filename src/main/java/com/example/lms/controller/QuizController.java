package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.entity.Quiz;
import com.example.lms.entity.QuizAttempt;
import com.example.lms.entity.QuizQuestion;
import com.example.lms.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService service;

    /* ================= CREATE QUIZ ================= */

    @PostMapping
    public ResponseEntity<Quiz> createQuiz(
            @RequestBody CreateQuizDTO dto
    ) {
        return ResponseEntity.ok(service.createQuiz(dto));
    }

    /* ================= UPDATE QUIZ ================= */

    @PutMapping("/{quizId}")
    public ResponseEntity<Quiz> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody CreateQuizDTO dto
    ) {
        return ResponseEntity.ok(service.updateQuiz(quizId, dto));
    }

    /* ================= ACTIVATE / DEACTIVATE ================= */

    @PutMapping("/{quizId}/activate")
    public ResponseEntity<Void> activateQuiz(@PathVariable Long quizId) {
        service.setQuizAvailability(quizId, true);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{quizId}/deactivate")
    public ResponseEntity<Void> deactivateQuiz(@PathVariable Long quizId) {
        service.setQuizAvailability(quizId, false);
        return ResponseEntity.ok().build();
    }

    /* ================= DELETE QUIZ ================= */

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        service.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    /* ================= ADD QUESTION ================= */

    @PostMapping("/questions")
    public ResponseEntity<QuizQuestion> addQuestion(
            @RequestBody CreateQuizQuestionDTO dto
    ) {
        return ResponseEntity.ok(service.addQuestion(dto));
    }

    /* ================= SUBMIT QUIZ ================= */

    @PostMapping("/submit")
    public ResponseEntity<QuizAttempt> submitQuiz(
            @RequestBody SubmitQuizDTO dto
    ) {
        return ResponseEntity.ok(service.submitQuiz(dto));
    }

    /* ================= GET ATTEMPTS ================= */

    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<List<QuizAttempt>> getAttempts(
            @PathVariable Long quizId
    ) {
        return ResponseEntity.ok(service.getAttempts(quizId));
    }

    /* ================= ATTEMPT COUNT ================= */

    @GetMapping("/{quizId}/attempts/{studentId}/count")
    public ResponseEntity<Long> getAttemptCount(
            @PathVariable Long quizId,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(
                service.getAttemptCount(quizId, studentId)
        );
    }

    /* ================= STUDENT RESULT ================= */

    @GetMapping("/{quizId}/result/{studentId}")
    public ResponseEntity<QuizResultDTO> getResult(
            @PathVariable Long quizId,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(
                service.getStudentResult(quizId, studentId)
        );
    }

    /* ================= TOPIC LOCK ================= */

    @GetMapping("/{quizId}/unlock/{studentId}")
    public ResponseEntity<Boolean> isTopicUnlocked(
            @PathVariable Long quizId,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(
                service.isTopicUnlocked(quizId, studentId)
        );
    }
}

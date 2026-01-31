package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepo;
    private final QuizQuestionRepository questionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final CourseTopicRepository topicRepo;

    /* ================= CREATE QUIZ ================= */

    public Quiz createQuiz(CreateQuizDTO dto) {

        CourseTopic topic = topicRepo.findById(dto.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        Quiz quiz = Quiz.builder()
                .title(dto.title())
                .topic(topic)
                .passMarks(dto.passMarks())
                .isActive(false) // locked by default
                .build();

        return quizRepo.save(quiz);
    }

    /* ================= UPDATE QUIZ ================= */

    public Quiz updateQuiz(Long quizId, CreateQuizDTO dto) {

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        quiz.setTitle(dto.title());
        quiz.setPassMarks(dto.passMarks());

        return quizRepo.save(quiz);
    }

    /* ================= ACTIVATE / DEACTIVATE ================= */

    public void setQuizAvailability(Long quizId, boolean active) {

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        quiz.setIsActive(active);
        quizRepo.save(quiz);
    }

    /* ================= DELETE QUIZ ================= */

    public void deleteQuiz(Long quizId) {

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        quizRepo.delete(quiz);
    }

    /* ================= ADD QUESTION ================= */

    public QuizQuestion addQuestion(CreateQuizQuestionDTO dto) {

        Quiz quiz = quizRepo.findById(dto.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        QuizQuestion question = QuizQuestion.builder()
                .question(dto.question())
                .optionA(dto.optionA())
                .optionB(dto.optionB())
                .optionC(dto.optionC())
                .optionD(dto.optionD())
                .correctAnswer(dto.correctAnswer())
                .quiz(quiz)
                .build();

        return questionRepo.save(question);
    }

    /* ================= SUBMIT QUIZ ================= */

    public QuizAttempt submitQuiz(SubmitQuizDTO dto) {

        Quiz quiz = quizRepo.findById(dto.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        if (!Boolean.TRUE.equals(quiz.getIsActive())) {
            throw new IllegalStateException("Quiz is not active");
        }

        long attemptCount =
                attemptRepo.countByQuizIdAndStudentId(dto.quizId(), dto.studentId());

        boolean passed = dto.score() >= quiz.getPassMarks();

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .studentId(dto.studentId())
                .attemptNumber((int) attemptCount + 1)
                .score(dto.score())
                .passed(passed)
                .attemptedAt(LocalDateTime.now())
                .build();

        return attemptRepo.save(attempt);
    }

    /* ================= GET ATTEMPTS ================= */

    @Transactional(readOnly = true)
    public List<QuizAttempt> getAttempts(Long quizId) {
        return attemptRepo.findByQuizId(quizId);
    }

    /* ================= STATS ================= */

    @Transactional(readOnly = true)
    public long getAttemptCount(Long quizId, Long studentId) {
        return attemptRepo.countByQuizIdAndStudentId(quizId, studentId);
    }

    @Transactional(readOnly = true)
    public QuizResultDTO getStudentResult(Long quizId, Long studentId) {

        QuizAttempt attempt =
                attemptRepo.findTopByQuizIdAndStudentIdOrderByAttemptNumberDesc(
                        quizId, studentId
                ).orElseThrow(() -> new IllegalArgumentException("No attempt found"));

        return new QuizResultDTO(
                attempt.getScore(),
                attempt.getPassed(),
                attempt.getAttemptNumber()
        );
    }

    /* ================= TOPIC LOCK ================= */

    @Transactional(readOnly = true)
    public boolean isTopicUnlocked(Long quizId, Long studentId) {

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        if (!Boolean.TRUE.equals(quiz.getIsActive())) {
            return false;
        }

        return attemptRepo
                .findTopByQuizIdAndStudentIdOrderByAttemptNumberDesc(quizId, studentId)
                .map(QuizAttempt::getPassed)
                .orElse(false);
    }
}

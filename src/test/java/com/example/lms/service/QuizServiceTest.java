package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService Test Suite")
public class QuizServiceTest {

    @Mock
    private QuizRepository quizRepo;

    @Mock
    private QuizQuestionRepository questionRepo;

    @Mock
    private QuizAttemptRepository attemptRepo;

    @Mock
    private CourseTopicRepository topicRepo;

    @InjectMocks
    private QuizService service;

    private CourseTopic topic;
    private Quiz quiz;
    private CreateQuizDTO createQuizDTO;
    private CreateQuizQuestionDTO createQuizQuestionDTO;
    private SubmitQuizDTO submitQuizDTO;
    private QuizQuestion quizQuestion;
    private QuizAttempt quizAttempt;

    @BeforeEach
    void setUp() {
        // Initialize topic
        topic = new CourseTopic();
        topic.setId(1L);
        topic.setTitle("Topic 1");

        // Initialize quiz
        quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Java Quiz");
        quiz.setPassMarks(70);
        quiz.setIsActive(true);
        quiz.setTopic(topic);

        // Initialize DTOs
        createQuizDTO = new CreateQuizDTO(1L, "Java Quiz", 70);
        createQuizQuestionDTO = new CreateQuizQuestionDTO(
                1L,
                "What is Java?",
                "A coffee brand",
                "A programming language",
                "An island",
                "A tool",
                "B"
        );
        submitQuizDTO = new SubmitQuizDTO(1L, 1L, 85);

        // Initialize question
        quizQuestion = new QuizQuestion();
        quizQuestion.setId(1L);
        quizQuestion.setQuestion("What is Java?");
        quizQuestion.setOptionA("A coffee brand");
        quizQuestion.setOptionB("A programming language");
        quizQuestion.setOptionC("An island");
        quizQuestion.setOptionD("A tool");
        quizQuestion.setCorrectAnswer("B");
        quizQuestion.setQuiz(quiz);

        // Initialize attempt
        quizAttempt = new QuizAttempt();
        quizAttempt.setId(1L);
        quizAttempt.setQuiz(quiz);
        quizAttempt.setStudentId(1L);
        quizAttempt.setScore(85);
        quizAttempt.setPassed(true);
        quizAttempt.setAttemptNumber(1);
        quizAttempt.setAttemptedAt(LocalDateTime.now());
    }

    // ===== CREATE QUIZ TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 1: Should create quiz successfully with valid topic")
    void testCreateQuiz_Success() {
        // Arrange
        Quiz newQuiz = new Quiz();
        newQuiz.setId(1L);
        newQuiz.setTitle("Java Quiz");
        newQuiz.setPassMarks(70);
        newQuiz.setIsActive(false); // locked by default
        newQuiz.setTopic(topic);
        
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(quizRepo.save(any(Quiz.class))).thenReturn(newQuiz);

        // Act
        Quiz result = service.createQuiz(createQuizDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Java Quiz");
        assertThat(result.getPassMarks()).isEqualTo(70);
        assertThat(result.getIsActive()).isFalse(); // locked by default
        verify(topicRepo, times(1)).findById(1L);
        verify(quizRepo, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test 2: Should create quiz with correct pass marks")
    void testCreateQuiz_VerifyPassMarks_Success() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(quizRepo.save(any(Quiz.class))).thenReturn(quiz);

        // Act
        Quiz result = service.createQuiz(createQuizDTO);

        // Assert
        assertThat(result.getPassMarks()).isEqualTo(70);
        verify(quizRepo).save(argThat(q -> q.getPassMarks() == 70));
    }

    // ===== CREATE QUIZ TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 3: Should throw exception when topic not found during quiz creation")
    void testCreateQuiz_TopicNotFound_Failure() {
        // Arrange
        when(topicRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.createQuiz(
                new CreateQuizDTO(999L, "Title", 70)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Topic not found");

        verify(quizRepo, never()).save(any());
    }

    // ===== UPDATE QUIZ TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 4: Should update quiz successfully")
    void testUpdateQuiz_Success() {
        // Arrange
        CreateQuizDTO updateDTO = new CreateQuizDTO(1L, "Updated Quiz", 80);
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepo.save(any(Quiz.class))).thenReturn(quiz);

        // Act
        Quiz result = service.updateQuiz(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(quizRepo, times(1)).findById(1L);
        verify(quizRepo, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test 5: Should verify quiz fields are updated correctly")
    void testUpdateQuiz_VerifyUpdate_Success() {
        // Arrange
        CreateQuizDTO updateDTO = new CreateQuizDTO(1L, "Updated Quiz", 80);
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepo.save(any(Quiz.class))).thenReturn(quiz);

        // Act
        service.updateQuiz(1L, updateDTO);

        // Assert
        verify(quizRepo).save(argThat(q ->
                q.getTitle().equals("Updated Quiz") && q.getPassMarks() == 80
        ));
    }

    // ===== UPDATE QUIZ TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 6: Should throw exception when updating non-existent quiz")
    void testUpdateQuiz_NotFound_Failure() {
        // Arrange
        when(quizRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateQuiz(999L, createQuizDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz not found");

        verify(quizRepo, never()).save(any());
    }

    // ===== SET QUIZ AVAILABILITY TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 7: Should activate quiz successfully")
    void testSetQuizAvailability_Activate_Success() {
        // Arrange
        quiz.setIsActive(false);
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepo.save(any(Quiz.class))).thenReturn(quiz);

        // Act
        service.setQuizAvailability(1L, true);

        // Assert
        verify(quizRepo, times(1)).findById(1L);
        verify(quizRepo, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Test 8: Should deactivate quiz successfully")
    void testSetQuizAvailability_Deactivate_Success() {
        // Arrange
        quiz.setIsActive(true);
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizRepo.save(any(Quiz.class))).thenReturn(quiz);

        // Act
        service.setQuizAvailability(1L, false);

        // Assert
        verify(quizRepo).save(argThat(q -> !q.getIsActive()));
    }

    // ===== SET QUIZ AVAILABILITY TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 9: Should throw exception when setting availability for non-existent quiz")
    void testSetQuizAvailability_NotFound_Failure() {
        // Arrange
        when(quizRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.setQuizAvailability(999L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz not found");

        verify(quizRepo, never()).save(any());
    }

    // ===== DELETE QUIZ TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 10: Should delete quiz successfully")
    void testDeleteQuiz_Success() {
        // Arrange
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));

        // Act
        service.deleteQuiz(1L);

        // Assert
        verify(quizRepo, times(1)).delete(quiz);
    }

    // ===== DELETE QUIZ TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 11: Should throw exception when deleting non-existent quiz")
    void testDeleteQuiz_NotFound_Failure() {
        // Arrange
        when(quizRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.deleteQuiz(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz not found");

        verify(quizRepo, never()).delete(any());
    }

    // ===== ADD QUESTION TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 12: Should add question to quiz successfully")
    void testAddQuestion_Success() {
        // Arrange
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(questionRepo.save(any(QuizQuestion.class))).thenReturn(quizQuestion);

        // Act
        QuizQuestion result = service.addQuestion(createQuizQuestionDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getQuestion()).isEqualTo("What is Java?");
        assertThat(result.getCorrectAnswer()).isEqualTo("B");
        verify(questionRepo, times(1)).save(any(QuizQuestion.class));
    }

    // ===== ADD QUESTION TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 13: Should throw exception when adding question to non-existent quiz")
    void testAddQuestion_QuizNotFound_Failure() {
        // Arrange
        when(quizRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.addQuestion(
                new CreateQuizQuestionDTO(999L, "Q", "A", "B", "C", "D", "A")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz not found");

        verify(questionRepo, never()).save(any());
    }

    // ===== SUBMIT QUIZ TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 14: Should submit quiz successfully when student passes")
    void testSubmitQuiz_Pass_Success() {
        // Arrange
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepo.countByQuizIdAndStudentId(1L, 1L)).thenReturn(0L);
        when(attemptRepo.save(any(QuizAttempt.class))).thenReturn(quizAttempt);

        // Act
        QuizAttempt result = service.submitQuiz(submitQuizDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPassed()).isTrue();
        assertThat(result.getScore()).isEqualTo(85);
        assertThat(result.getAttemptNumber()).isEqualTo(1);
        verify(attemptRepo, times(1)).save(any(QuizAttempt.class));
    }

    @Test
    @DisplayName("Test 15: Should submit quiz when student fails")
    void testSubmitQuiz_Fail_Success() {
        // Arrange
        SubmitQuizDTO failDTO = new SubmitQuizDTO(1L, 1L, 50);
        QuizAttempt failedAttempt = new QuizAttempt();
        failedAttempt.setId(1L);
        failedAttempt.setQuiz(quiz);
        failedAttempt.setStudentId(1L);
        failedAttempt.setScore(50);
        failedAttempt.setPassed(false);
        failedAttempt.setAttemptNumber(1);

        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepo.countByQuizIdAndStudentId(1L, 1L)).thenReturn(0L);
        when(attemptRepo.save(any(QuizAttempt.class))).thenReturn(failedAttempt);

        // Act
        QuizAttempt result = service.submitQuiz(failDTO);

        // Assert
        assertThat(result.getPassed()).isFalse();
        assertThat(result.getScore()).isEqualTo(50);
    }

    // ===== SUBMIT QUIZ TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 16: Should throw exception when submitting quiz that is not active")
    void testSubmitQuiz_InactiveQuiz_Failure() {
        // Arrange
        quiz.setIsActive(false);
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));

        // Act & Assert
        assertThatThrownBy(() -> service.submitQuiz(submitQuizDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Quiz is not active");

        verify(attemptRepo, never()).save(any());
    }

    @Test
    @DisplayName("Test 17: Should throw exception when submitting quiz that does not exist")
    void testSubmitQuiz_QuizNotFound_Failure() {
        // Arrange
        when(quizRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.submitQuiz(
                new SubmitQuizDTO(999L, 1L, 85)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz not found");

        verify(attemptRepo, never()).save(any());
    }

    // ===== GET ATTEMPTS TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 18: Should retrieve all attempts for a quiz successfully")
    void testGetAttempts_Success() {
        // Arrange
        QuizAttempt attempt2 = new QuizAttempt();
        attempt2.setId(2L);
        attempt2.setAttemptNumber(2);
        attempt2.setScore(75);

        when(attemptRepo.findByQuizId(1L)).thenReturn(Arrays.asList(quizAttempt, attempt2));

        // Act
        List<QuizAttempt> results = service.getAttempts(1L);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getScore()).isEqualTo(85);
        assertThat(results.get(1).getScore()).isEqualTo(75);
        verify(attemptRepo, times(1)).findByQuizId(1L);
    }

    // ===== ATTEMPT COUNT TESTS - HAPPY PATH =====

    @Test
    @DisplayName("Test 19: Should get attempt count for student successfully")
    void testGetAttemptCount_Success() {
        // Arrange
        when(attemptRepo.countByQuizIdAndStudentId(1L, 1L)).thenReturn(3L);

        // Act
        long result = service.getAttemptCount(1L, 1L);

        // Assert
        assertThat(result).isEqualTo(3L);
        verify(attemptRepo, times(1)).countByQuizIdAndStudentId(1L, 1L);
    }

    // ===== IS TOPIC UNLOCKED TESTS - UNHAPPY PATH =====

    @Test
    @DisplayName("Test 20: Should return false when quiz is inactive or student has not passed")
    void testIsTopicUnlocked_InactiveOrNotPassed_Failure() {
        // Arrange
        quiz.setIsActive(false);
        when(quizRepo.findById(1L)).thenReturn(Optional.of(quiz));

        // Act
        boolean result = service.isTopicUnlocked(1L, 1L);

        // Assert
        assertThat(result).isFalse();
    }
}

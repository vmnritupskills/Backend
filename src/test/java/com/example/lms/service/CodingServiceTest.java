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
@DisplayName("CodingService Test Suite")
public class CodingServiceTest {

    @Mock
    private CodingQuestionRepository questionRepo;

    @Mock
    private CodingTestCaseRepository testCaseRepo;

    @Mock
    private CodingSubmissionRepository submissionRepo;

    @Mock
    private CourseTopicRepository topicRepo;

    @Mock
    private StudentRepository studentRepo;

    @InjectMocks
    private CodingService service;

    private CreateCodingQuestionDTO createQuestionDTO;
    private UpdateCodingQuestionDTO updateQuestionDTO;
    private CreateTestCaseDTO createTestCaseDTO;
    private UpdateTestCaseDTO updateTestCaseDTO;
    private CourseTopic topic;
    private CodingQuestion question;
    private CodingTestCase testCase;

    @BeforeEach
    void setUp() {
        // Initialize topic
        topic = new CourseTopic();
        topic.setId(1L);
        topic.setTitle("Topic 1");

        // Initialize question
        question = new CodingQuestion();
        question.setId(1L);
        question.setTitle("Two Sum Problem");
        question.setDescription("Find two numbers that add up to target");
        question.setExampleInput("[1, 2, 3]\ntarget = 5");
        question.setExampleOutput("[1, 2]");
        question.setTotalTestCases(0);
        question.setTopic(topic);

        // Initialize test case
        testCase = new CodingTestCase();
        testCase.setId(1L);
        testCase.setInput("[1, 2, 3]");
        testCase.setExpectedOutput("0 1");
        testCase.setHidden(false);
        testCase.setCodingQuestion(question);

        // Initialize DTOs
        createQuestionDTO = new CreateCodingQuestionDTO(
                1L,
                "Two Sum Problem",
                "Find two numbers that add up to target",
                "[1, 2, 3]\ntarget = 5",
                "[1, 2]"
        );

        updateQuestionDTO = new UpdateCodingQuestionDTO(
                "Two Sum Problem - Updated",
                "Updated description",
                "[1, 2, 3, 4]\ntarget = 7",
                "[3, 4]"
        );

        createTestCaseDTO = new CreateTestCaseDTO(
                "[1, 2, 3]",
                "0 1",
                false
        );

        updateTestCaseDTO = new UpdateTestCaseDTO(
                "[1, 2, 3, 4]",
                "0 3",
                true
        );
    }

    // ===== CREATE QUESTION TESTS =====

    @Test
    @DisplayName("Test 1: Should create a question successfully")
    void testCreateQuestion_Success() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        CodingQuestionResponseDTO result = service.createQuestion(createQuestionDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Two Sum Problem");
        assertThat(result.topicId()).isEqualTo(1L);
        verify(topicRepo, times(1)).findById(1L);
        verify(questionRepo, times(1)).save(any(CodingQuestion.class));
    }

    @Test
    @DisplayName("Test 2: Should throw exception when topic not found during creation")
    void testCreateQuestion_TopicNotFound() {
        // Arrange
        when(topicRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.createQuestion(
                new CreateCodingQuestionDTO(999L, "Title", "Desc", "In", "Out")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Topic not found");

        verify(topicRepo, times(1)).findById(999L);
        verify(questionRepo, never()).save(any());
    }

    @Test
    @DisplayName("Test 3: Should verify question is saved with correct topic")
    void testCreateQuestion_VerifyTopicAssignment() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        service.createQuestion(createQuestionDTO);

        // Assert
        verify(questionRepo).save(argThat(q ->
                q.getTopic().getId().equals(1L) &&
                        q.getTitle().equals("Two Sum Problem") &&
                        q.getTotalTestCases() == 0
        ));
    }

    // ===== GET QUESTION TESTS =====

    @Test
    @DisplayName("Test 4: Should retrieve all questions successfully")
    void testGetAllQuestions_Success() {
        // Arrange
        CodingQuestion question2 = new CodingQuestion();
        question2.setId(2L);
        question2.setTitle("Array Rotation");
        question2.setTopic(topic);
        
        when(questionRepo.findAll()).thenReturn(Arrays.asList(question, question2));

        // Act
        List<CodingQuestionResponseDTO> results = service.getAllQuestions();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).title()).isEqualTo("Two Sum Problem");
        assertThat(results.get(1).title()).isEqualTo("Array Rotation");
        verify(questionRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 5: Should retrieve single question by ID")
    void testGetQuestion_ById_Success() {
        // Arrange
        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));

        // Act
        CodingQuestionResponseDTO result = service.getQuestion(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Two Sum Problem");
        verify(questionRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test 6: Should throw exception when retrieving non-existent question")
    void testGetQuestion_NotFound() {
        // Arrange
        when(questionRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getQuestion(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question not found");

        verify(questionRepo, times(1)).findById(999L);
    }

    // ===== UPDATE QUESTION TESTS =====

    @Test
    @DisplayName("Test 7: Should update question successfully")
    void testUpdateQuestion_Success() {
        // Arrange
        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        CodingQuestionResponseDTO result = service.updateQuestion(1L, updateQuestionDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(questionRepo, times(1)).findById(1L);
        verify(questionRepo, times(1)).save(any(CodingQuestion.class));
    }

    @Test
    @DisplayName("Test 8: Should verify question fields are updated correctly")
    void testUpdateQuestion_FieldsUpdated() {
        // Arrange
        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        service.updateQuestion(1L, updateQuestionDTO);

        // Assert
        verify(questionRepo).save(argThat(q ->
                q.getTitle().equals("Two Sum Problem - Updated") &&
                        q.getDescription().equals("Updated description")
        ));
    }

    @Test
    @DisplayName("Test 9: Should throw exception when updating non-existent question")
    void testUpdateQuestion_NotFound() {
        // Arrange
        when(questionRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateQuestion(999L, updateQuestionDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question not found");

        verify(questionRepo, times(1)).findById(999L);
        verify(questionRepo, never()).save(any());
    }

    // ===== DELETE QUESTION TESTS =====

    @Test
    @DisplayName("Test 10: Should delete question and associated data successfully")
    void testDeleteQuestion_Success() {
        // Arrange
        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));
        when(testCaseRepo.findByCodingQuestionId(1L)).thenReturn(Arrays.asList(testCase));
        when(submissionRepo.findByCodingQuestionId(1L)).thenReturn(List.of());

        // Act
        service.deleteQuestion(1L);

        // Assert
        verify(questionRepo, times(1)).findById(1L);
        verify(testCaseRepo, times(1)).deleteAll(any());
        verify(submissionRepo, times(1)).deleteAll(any());
        verify(questionRepo, times(1)).delete(question);
    }

    @Test
    @DisplayName("Test 11: Should throw exception when deleting non-existent question")
    void testDeleteQuestion_NotFound() {
        // Arrange
        when(questionRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.deleteQuestion(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question not found");

        verify(questionRepo, never()).delete(any());
    }

    // ===== ADD TEST CASE TESTS =====

    @Test
    @DisplayName("Test 12: Should add test case successfully")
    void testAddTestCase_Success() {
        // Arrange
        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));
        when(testCaseRepo.save(any(CodingTestCase.class))).thenReturn(testCase);
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        CodingTestCaseResponseDTO result = service.addTestCase(1L, createTestCaseDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.input()).isEqualTo("[1, 2, 3]");
        verify(testCaseRepo, times(1)).save(any(CodingTestCase.class));
        verify(questionRepo, times(1)).save(any(CodingQuestion.class));
    }

    @Test
    @DisplayName("Test 13: Should increment total test cases when adding test case")
    void testAddTestCase_IncrementCount() {
        // Arrange
        question.setTotalTestCases(2);
        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));
        when(testCaseRepo.save(any(CodingTestCase.class))).thenReturn(testCase);
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        service.addTestCase(1L, createTestCaseDTO);

        // Assert
        verify(questionRepo).save(argThat(q -> q.getTotalTestCases() == 3));
    }

    @Test
    @DisplayName("Test 14: Should throw exception when adding test case to non-existent question")
    void testAddTestCase_QuestionNotFound() {
        // Arrange
        when(questionRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.addTestCase(999L, createTestCaseDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question not found");

        verify(testCaseRepo, never()).save(any());
    }

    // ===== GET TEST CASES TESTS =====

    @Test
    @DisplayName("Test 15: Should retrieve all test cases for a question")
    void testGetAllTestCases_Success() {
        // Arrange
        CodingTestCase testCase2 = new CodingTestCase();
        testCase2.setId(2L);
        testCase2.setInput("[2, 3, 5]");
        testCase2.setExpectedOutput("1 2");
        testCase2.setHidden(true);
        testCase2.setCodingQuestion(question);

        when(questionRepo.findById(1L)).thenReturn(Optional.of(question));
        when(testCaseRepo.findByCodingQuestionId(1L)).thenReturn(Arrays.asList(testCase, testCase2));

        // Act
        List<CodingTestCaseResponseDTO> results = service.getAllTestCases(1L);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo(1L);
        assertThat(results.get(1).id()).isEqualTo(2L);
        verify(testCaseRepo, times(1)).findByCodingQuestionId(1L);
    }

    @Test
    @DisplayName("Test 16: Should throw exception when retrieving test cases for non-existent question")
    void testGetAllTestCases_QuestionNotFound() {
        // Arrange
        when(questionRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getAllTestCases(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question not found");

        verify(testCaseRepo, never()).findByCodingQuestionId(any());
    }

    // ===== UPDATE TEST CASE TESTS =====

    @Test
    @DisplayName("Test 17: Should update test case successfully")
    void testUpdateTestCase_Success() {
        // Arrange
        when(testCaseRepo.findById(1L)).thenReturn(Optional.of(testCase));
        when(testCaseRepo.save(any(CodingTestCase.class))).thenReturn(testCase);

        // Act
        CodingTestCaseResponseDTO result = service.updateTestCase(1L, updateTestCaseDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(testCaseRepo, times(1)).findById(1L);
        verify(testCaseRepo, times(1)).save(any(CodingTestCase.class));
    }

    @Test
    @DisplayName("Test 18: Should throw exception when updating non-existent test case")
    void testUpdateTestCase_NotFound() {
        // Arrange
        when(testCaseRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateTestCase(999L, updateTestCaseDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test case not found");

        verify(testCaseRepo, never()).save(any());
    }

    // ===== DELETE TEST CASE TESTS =====

    @Test
    @DisplayName("Test 19: Should delete test case successfully")
    void testDeleteTestCase_Success() {
        // Arrange
        when(testCaseRepo.findById(1L)).thenReturn(Optional.of(testCase));
        question.setTotalTestCases(5);
        when(questionRepo.save(any(CodingQuestion.class))).thenReturn(question);

        // Act
        service.deleteTestCase(1L);

        // Assert
        verify(testCaseRepo, times(1)).delete(testCase);
        verify(questionRepo, times(1)).save(argThat(q -> q.getTotalTestCases() == 4));
    }

    @Test
    @DisplayName("Test 20: Should throw exception when deleting non-existent test case")
    void testDeleteTestCase_NotFound() {
        // Arrange
        when(testCaseRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.deleteTestCase(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Test case not found");

        verify(testCaseRepo, never()).delete(any());
    }
}

package com.example.lms.service;

import com.example.lms.controller.CodingCMController;
import com.example.lms.dto.*;
import com.example.lms.entity.CourseTopic;
import com.example.lms.entity.CodingQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodingCMController Test Suite")
public class CodingCMControllerTesting {

    @Mock
    private CodingService service;

    @InjectMocks
    private CodingCMController controller;

    private CreateCodingQuestionDTO createQuestionDTO;
    private UpdateCodingQuestionDTO updateQuestionDTO;
    private CodingQuestionResponseDTO questionResponseDTO;
    private CreateTestCaseDTO createTestCaseDTO;
    private UpdateTestCaseDTO updateTestCaseDTO;
    private CodingTestCaseResponseDTO testCaseResponseDTO;
    private CMSubmissionViewDTO submissionViewDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data for CreateCodingQuestionDTO
        createQuestionDTO = new CreateCodingQuestionDTO(
                1L,
                "Two Sum Problem",
                "Find two numbers that add up to target",
                "[1, 2, 3]\ntarget = 5",
                "[1, 2]"
        );

        // Initialize test data for UpdateCodingQuestionDTO
        updateQuestionDTO = new UpdateCodingQuestionDTO(
                "Two Sum Problem - Updated",
                "Updated description",
                "[1, 2, 3, 4]\ntarget = 7",
                "[3, 4]"
        );

        // Initialize test data for CodingQuestionResponseDTO
        questionResponseDTO = new CodingQuestionResponseDTO(
                1L,
                "Two Sum Problem",
                "Find two numbers that add up to target",
                "[1, 2, 3]\ntarget = 5",
                "[1, 2]",
                1L
        );

        // Initialize test data for CreateTestCaseDTO
        createTestCaseDTO = new CreateTestCaseDTO(
                "[1, 2, 3]",
                "0 1",
                false
        );

        // Initialize test data for UpdateTestCaseDTO
        updateTestCaseDTO = new UpdateTestCaseDTO(
                "[1, 2, 3, 4]",
                "0 3",
                true
        );

        // Initialize test data for CodingTestCaseResponseDTO
        testCaseResponseDTO = new CodingTestCaseResponseDTO(
                1L,
                "[1, 2, 3]",
                "0 1",
                false,
                1L
        );

        // Initialize test data for CMSubmissionViewDTO
        submissionViewDTO = new CMSubmissionViewDTO(
                1L,
                "John Doe",
                5,
                5,
                true,
                LocalDateTime.now()
        );
    }

    // ===== CREATE QUESTION TESTS =====

    @Test
    @DisplayName("Test 1: Should create a coding question successfully")
    void testCreateQuestion_Success() {
        // Arrange
        when(service.createQuestion(any(CreateCodingQuestionDTO.class)))
                .thenReturn(questionResponseDTO);

        // Act
        ResponseEntity<CodingQuestionResponseDTO> response = controller.createQuestion(createQuestionDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().title()).isEqualTo("Two Sum Problem");
        verify(service, times(1)).createQuestion(any(CreateCodingQuestionDTO.class));
    }

    @Test
    @DisplayName("Test 2: Should throw exception when creating question with invalid topic")
    void testCreateQuestion_InvalidTopic() {
        // Arrange
        when(service.createQuestion(any(CreateCodingQuestionDTO.class)))
                .thenThrow(new IllegalArgumentException("Topic not found"));

        // Act & Assert
        try {
            controller.createQuestion(createQuestionDTO);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Topic not found");
        }
        verify(service, times(1)).createQuestion(any(CreateCodingQuestionDTO.class));
    }

    // ===== GET QUESTIONS TESTS =====

    @Test
    @DisplayName("Test 3: Should retrieve all coding questions")
    void testGetAllQuestions_Success() {
        // Arrange
        List<CodingQuestionResponseDTO> questions = Arrays.asList(
                questionResponseDTO,
                new CodingQuestionResponseDTO(2L, "Array Rotation", "Rotate array", "[1,2,3]", "[2,3,1]", 1L)
        );
        when(service.getAllQuestions()).thenReturn(questions);

        // Act
        ResponseEntity<List<CodingQuestionResponseDTO>> response = controller.getAllQuestions();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).title()).isEqualTo("Two Sum Problem");
        verify(service, times(1)).getAllQuestions();
    }

    @Test
    @DisplayName("Test 4: Should return empty list when no questions exist")
    void testGetAllQuestions_EmptyList() {
        // Arrange
        when(service.getAllQuestions()).thenReturn(List.of());

        // Act
        ResponseEntity<List<CodingQuestionResponseDTO>> response = controller.getAllQuestions();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(service, times(1)).getAllQuestions();
    }

    @Test
    @DisplayName("Test 5: Should retrieve a specific question by ID")
    void testGetQuestion_ById_Success() {
        // Arrange
        when(service.getQuestion(1L)).thenReturn(questionResponseDTO);

        // Act
        ResponseEntity<CodingQuestionResponseDTO> response = controller.getQuestion(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().title()).isEqualTo("Two Sum Problem");
        verify(service, times(1)).getQuestion(1L);
    }

    @Test
    @DisplayName("Test 6: Should throw exception when retrieving non-existent question")
    void testGetQuestion_ById_NotFound() {
        // Arrange
        when(service.getQuestion(999L))
                .thenThrow(new IllegalArgumentException("Question not found"));

        // Act & Assert
        try {
            controller.getQuestion(999L);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Question not found");
        }
        verify(service, times(1)).getQuestion(999L);
    }

    // ===== UPDATE QUESTION TESTS =====

    @Test
    @DisplayName("Test 7: Should update a coding question successfully")
    void testUpdateQuestion_Success() {
        // Arrange
        CodingQuestionResponseDTO updatedResponse = new CodingQuestionResponseDTO(
                1L,
                "Two Sum Problem - Updated",
                "Updated description",
                "[1, 2, 3, 4]\ntarget = 7",
                "[3, 4]",
                1L
        );
        when(service.updateQuestion(eq(1L), any(UpdateCodingQuestionDTO.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<CodingQuestionResponseDTO> response = controller.updateQuestion(1L, updateQuestionDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo("Two Sum Problem - Updated");
        verify(service, times(1)).updateQuestion(eq(1L), any(UpdateCodingQuestionDTO.class));
    }

    @Test
    @DisplayName("Test 8: Should throw exception when updating non-existent question")
    void testUpdateQuestion_NotFound() {
        // Arrange
        when(service.updateQuestion(eq(999L), any(UpdateCodingQuestionDTO.class)))
                .thenThrow(new IllegalArgumentException("Question not found"));

        // Act & Assert
        try {
            controller.updateQuestion(999L, updateQuestionDTO);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Question not found");
        }
        verify(service, times(1)).updateQuestion(eq(999L), any(UpdateCodingQuestionDTO.class));
    }

    // ===== DELETE QUESTION TESTS =====

    @Test
    @DisplayName("Test 9: Should delete a coding question successfully")
    void testDeleteQuestion_Success() {
        // Arrange
        doNothing().when(service).deleteQuestion(1L);

        // Act
        ResponseEntity<Void> response = controller.deleteQuestion(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(service, times(1)).deleteQuestion(1L);
    }

    @Test
    @DisplayName("Test 10: Should throw exception when deleting non-existent question")
    void testDeleteQuestion_NotFound() {
        // Arrange
        doThrow(new IllegalArgumentException("Question not found"))
                .when(service).deleteQuestion(999L);

        // Act & Assert
        try {
            controller.deleteQuestion(999L);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Question not found");
        }
        verify(service, times(1)).deleteQuestion(999L);
    }

    // ===== TEST CASES TESTS =====

    @Test
    @DisplayName("Test 11: Should retrieve all test cases for a question")
    void testGetAllTestCases_Success() {
        // Arrange
        List<CodingTestCaseResponseDTO> testCases = Arrays.asList(
                testCaseResponseDTO,
                new CodingTestCaseResponseDTO(2L, "[2, 3, 5]", "1 2", true, 1L)
        );
        when(service.getAllTestCases(1L)).thenReturn(testCases);

        // Act
        ResponseEntity<List<CodingTestCaseResponseDTO>> response = controller.getAllTestCases(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(service, times(1)).getAllTestCases(1L);
    }

    @Test
    @DisplayName("Test 12: Should return empty test cases list when question has no test cases")
    void testGetAllTestCases_EmptyList() {
        // Arrange
        when(service.getAllTestCases(1L)).thenReturn(List.of());

        // Act
        ResponseEntity<List<CodingTestCaseResponseDTO>> response = controller.getAllTestCases(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(service, times(1)).getAllTestCases(1L);
    }

    @Test
    @DisplayName("Test 13: Should add a test case to a question successfully")
    void testAddTestCase_Success() {
        // Arrange
        when(service.addTestCase(eq(1L), any(CreateTestCaseDTO.class)))
                .thenReturn(testCaseResponseDTO);

        // Act
        ResponseEntity<CodingTestCaseResponseDTO> response = controller.addTestCase(1L, createTestCaseDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().input()).isEqualTo("[1, 2, 3]");
        verify(service, times(1)).addTestCase(eq(1L), any(CreateTestCaseDTO.class));
    }

    @Test
    @DisplayName("Test 14: Should throw exception when adding test case to non-existent question")
    void testAddTestCase_QuestionNotFound() {
        // Arrange
        when(service.addTestCase(eq(999L), any(CreateTestCaseDTO.class)))
                .thenThrow(new IllegalArgumentException("Question not found"));

        // Act & Assert
        try {
            controller.addTestCase(999L, createTestCaseDTO);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Question not found");
        }
        verify(service, times(1)).addTestCase(eq(999L), any(CreateTestCaseDTO.class));
    }

    @Test
    @DisplayName("Test 15: Should update a test case successfully")
    void testUpdateTestCase_Success() {
        // Arrange
        CodingTestCaseResponseDTO updatedTestCase = new CodingTestCaseResponseDTO(
                1L,
                "[1, 2, 3, 4]",
                "0 3",
                true,
                1L
        );
        when(service.updateTestCase(eq(1L), any(UpdateTestCaseDTO.class)))
                .thenReturn(updatedTestCase);

        // Act
        ResponseEntity<CodingTestCaseResponseDTO> response = controller.updateTestCase(1L, updateTestCaseDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().hidden()).isEqualTo(true);
        verify(service, times(1)).updateTestCase(eq(1L), any(UpdateTestCaseDTO.class));
    }

    @Test
    @DisplayName("Test 16: Should throw exception when updating non-existent test case")
    void testUpdateTestCase_NotFound() {
        // Arrange
        when(service.updateTestCase(eq(999L), any(UpdateTestCaseDTO.class)))
                .thenThrow(new IllegalArgumentException("Test case not found"));

        // Act & Assert
        try {
            controller.updateTestCase(999L, updateTestCaseDTO);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Test case not found");
        }
        verify(service, times(1)).updateTestCase(eq(999L), any(UpdateTestCaseDTO.class));
    }

    @Test
    @DisplayName("Test 17: Should delete a test case successfully")
    void testDeleteTestCase_Success() {
        // Arrange
        doNothing().when(service).deleteTestCase(1L);

        // Act
        ResponseEntity<Void> response = controller.deleteTestCase(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service, times(1)).deleteTestCase(1L);
    }

    @Test
    @DisplayName("Test 18: Should throw exception when deleting non-existent test case")
    void testDeleteTestCase_NotFound() {
        // Arrange
        doThrow(new IllegalArgumentException("Test case not found"))
                .when(service).deleteTestCase(999L);

        // Act & Assert
        try {
            controller.deleteTestCase(999L);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Test case not found");
        }
        verify(service, times(1)).deleteTestCase(999L);
    }

    // ===== SUBMISSIONS TESTS =====

    @Test
    @DisplayName("Test 19: Should retrieve all submissions for a question")
    void testViewSubmissions_Success() {
        // Arrange
        List<CMSubmissionViewDTO> submissions = Arrays.asList(
                submissionViewDTO,
                new CMSubmissionViewDTO(2L, "Jane Smith", 3, 5, false, LocalDateTime.now())
        );
        when(service.getSubmissions(1L)).thenReturn(submissions);

        // Act
        ResponseEntity<?> response = controller.viewSubmissions(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).hasSize(2);
        verify(service, times(1)).getSubmissions(1L);
    }

    @Test
    @DisplayName("Test 20: Should return empty submissions list when question has no submissions")
    void testViewSubmissions_EmptyList() {
        // Arrange
        when(service.getSubmissions(1L)).thenReturn(List.of());

        // Act
        ResponseEntity<?> response = controller.viewSubmissions(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) response.getBody()).isEmpty();
        verify(service, times(1)).getSubmissions(1L);
    }
}

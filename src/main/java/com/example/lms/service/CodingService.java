package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CodingService {

    private final CodingQuestionRepository questionRepo;
    private final CodingTestCaseRepository testCaseRepo;
    private final CodingSubmissionRepository submissionRepo;
    private final CourseTopicRepository topicRepo;
    private final StudentRepository studentRepo;

    /* ===== CM ===== */

    public CodingQuestionResponseDTO createQuestion(CreateCodingQuestionDTO dto) {

        CourseTopic topic = topicRepo.findById(dto.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        CodingQuestion question = questionRepo.save(
                CodingQuestion.builder()
                        .title(dto.title())
                        .description(dto.description())
                        .exampleInput(dto.exampleInput())
                        .exampleOutput(dto.exampleOutput())
                        .totalTestCases(0)
                        .topic(topic)
                        .build()
        );

        return new CodingQuestionResponseDTO(
                question.getId(),
                question.getTitle(),
                question.getDescription(),
                question.getExampleInput(),
                question.getExampleOutput(),
                topic.getId()
        );
    }


    public CodingTestCaseResponseDTO addTestCase(Long questionId, CreateTestCaseDTO dto) {

        CodingQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        CodingTestCase testCase = testCaseRepo.save(
                CodingTestCase.builder()
                        .input(dto.input())
                        .expectedOutput(dto.expectedOutput())
                        .hidden(dto.hidden())
                        .codingQuestion(question)
                        .build()
        );

        Integer current = question.getTotalTestCases();
        question.setTotalTestCases(current == null ? 1 : current + 1);
        questionRepo.save(question);

        return new CodingTestCaseResponseDTO(
                testCase.getId(),
                testCase.getInput(),
                testCase.getExpectedOutput(),
                testCase.getHidden(),
                question.getId()
        );
    }
    public CodingQuestionResponseDTO updateQuestion(
            Long questionId,
            UpdateCodingQuestionDTO dto
    ) {
        CodingQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        question.setTitle(dto.title());
        question.setDescription(dto.description());
        question.setExampleInput(dto.exampleInput());
        question.setExampleOutput(dto.exampleOutput());

        questionRepo.save(question);

        return new CodingQuestionResponseDTO(
                question.getId(),
                question.getTitle(),
                question.getDescription(),
                question.getExampleInput(),
                question.getExampleOutput(),
                question.getTopic().getId()
        );
    }

    public void deleteQuestion(Long questionId) {

        CodingQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        testCaseRepo.deleteAll(
                testCaseRepo.findByCodingQuestionId(questionId)
        );

        submissionRepo.deleteAll(
                submissionRepo.findByCodingQuestionId(questionId)
        );

        questionRepo.delete(question);
    }




    public List<CMSubmissionViewDTO> getSubmissions(Long questionId) {

        return submissionRepo.findByCodingQuestionId(questionId)
                .stream()
                .map(s -> new CMSubmissionViewDTO(
                        s.getStudent().getId(),
                        s.getStudent().getName(),
                        s.getPassedTestCases(),
                        s.getTotalTestCases(),
                        s.getCompleted(),
                        s.getSubmittedAt()
                ))
                .toList();
    }

    public List<CodingQuestionResponseDTO> getAllQuestions() {
        return questionRepo.findAll()
                .stream()
                .map(q -> new CodingQuestionResponseDTO(
                        q.getId(),
                        q.getTitle(),
                        q.getDescription(),
                        q.getExampleInput(),
                        q.getExampleOutput(),
                        q.getTopic().getId()
                ))
                .toList();
    }
    public CodingQuestionResponseDTO getQuestion(Long id) {
        CodingQuestion q = questionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        return new CodingQuestionResponseDTO(
                q.getId(),
                q.getTitle(),
                q.getDescription(),
                q.getExampleInput(),
                q.getExampleOutput(),
                q.getTopic().getId()
        );
    }



    /* ===== STUDENT ===== */

    public SubmissionResultDTO submitCode(
            Long questionId,
            Long studentId,
            SubmitCodeDTO dto
    ) {

        if (submissionRepo.existsByCodingQuestionIdAndStudentId(questionId, studentId)) {
            throw new IllegalStateException("Already submitted");
        }

        CodingQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<CodingTestCase> testCases =
                testCaseRepo.findByCodingQuestionId(questionId);

        if (testCases.isEmpty()) {
            throw new IllegalStateException("No test cases configured");
        }

        int passed = runCodeAndReturnPassedCount(
                dto.sourceCode(),
                dto.language(),
                testCases
        );

        int total = testCases.size();

        CodingSubmission submission = submissionRepo.save(
                CodingSubmission.builder()
                        .codingQuestion(question)
                        .student(student)
                        .sourceCode(dto.sourceCode())
                        .passedTestCases(passed)
                        .totalTestCases(total)
                        .completed(passed == total)
                        .submittedAt(LocalDateTime.now())
                        .build()
        );

        return new SubmissionResultDTO(
                passed,
                total,
                submission.getCompleted()
        );
    }

    public CodingTestCaseResponseDTO updateTestCase(
            Long testCaseId,
            UpdateTestCaseDTO dto
    ) {
        CodingTestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new IllegalArgumentException("Test case not found"));

        testCase.setInput(dto.input());
        testCase.setExpectedOutput(dto.expectedOutput());
        testCase.setHidden(dto.hidden());

        testCaseRepo.save(testCase);

        return new CodingTestCaseResponseDTO(
                testCase.getId(),
                testCase.getInput(),
                testCase.getExpectedOutput(),
                testCase.getHidden(),
                testCase.getCodingQuestion().getId()
        );
    }
    public List<CodingTestCaseResponseDTO> getAllTestCases(Long questionId) {

        CodingQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        return testCaseRepo.findByCodingQuestionId(questionId)
                .stream()
                .map(tc -> new CodingTestCaseResponseDTO(
                        tc.getId(),
                        tc.getInput(),
                        tc.getExpectedOutput(),
                        tc.getHidden(),
                        questionId
                ))
                .toList();
    }


    public void deleteTestCase(Long testCaseId) {

        CodingTestCase testCase = testCaseRepo.findById(testCaseId)
                .orElseThrow(() -> new IllegalArgumentException("Test case not found"));

        CodingQuestion question = testCase.getCodingQuestion();

        testCaseRepo.delete(testCase);

        Integer current = question.getTotalTestCases();
        question.setTotalTestCases(
                current != null && current > 0 ? current - 1 : 0
        );

        questionRepo.save(question);
    }



    /* ===== PLACEHOLDER ===== */
    private int runCodeAndReturnPassedCount(
            String sourceCode,
            String language,
            List<CodingTestCase> testCases
    ) {
        // TODO: integrate Judge0 / Docker
        return 0;
    }
}


package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.Exam;
import com.example.lms.entity.ExamQuestion;
import com.example.lms.dto.ExamStatus;
import com.example.lms.exception.BadRequestException;
import com.example.lms.repository.ExamQuestionRepository;
import com.example.lms.repository.ExamRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final AiQuestionService aiQuestionService;
    private final AiDocumentService aiDocumentService;
    private final ObjectMapper objectMapper;

    /* =========================================================
                       CREATE EXAM
       ========================================================= */
    @Transactional
    public Exam createExam(CreateExamRequest request) throws JsonProcessingException {

        validateRequest(request);

        log.info("Creating exam: {}", request.getTitle());

        Exam exam = examRepository.save(
                Exam.builder()
                        .title(request.getTitle())
                        .startDate(request.getStartDate())
                        .startTime(request.getStartTime())
                        .durationMinutes(request.getDurationMinutes())
                        .courseId(request.getCourseId())
                        .status(ExamStatus.DRAFT)
                        .build()
        );

        ProcessedContentDTO processed =
                aiDocumentService.uploadDocument(request.getDocument());

        if (processed == null ||
                processed.getChunks() == null ||
                processed.getChunks().isEmpty()) {
            throw new BadRequestException("No content extracted from document");
        }

        List<String> chunks =
                processed.getChunks().subList(
                        0,
                        Math.min(3, processed.getChunks().size())
                );

        int totalQuestionsSaved = 0;

        for (String chunk : chunks) {

            if (chunk == null || chunk.length() < 50) {
                continue;
            }

            AiQuestionGenerationResponseDTO aiResponse =
                    aiQuestionService.generateFromChunk(
                            chunk,
                            List.of("mcq", "true_false"),
                            3,
                            Map.of(
                                    "easy", 0.4,
                                    "medium", 0.4,
                                    "hard", 0.2
                            )
                    );

            if (aiResponse == null ||
                    aiResponse.getSuccess() == null ||
                    !aiResponse.getSuccess() ||
                    aiResponse.getQuestions() == null ||
                    aiResponse.getQuestions().isEmpty()) {
                continue;
            }

            for (AiQuestionDTO q : aiResponse.getQuestions()) {

                ExamQuestion examQuestion = ExamQuestion.builder()
                        .examId(exam.getId())
                        .questionType(q.getQuestionType())
                        .question(q.getQuestion())
                        .correctAnswer(q.getCorrectAnswer())
                        .difficulty(q.getDifficulty())
                        .topic(q.getTopic())
                        .optionsJson(
                                q.getOptions() != null
                                        ? objectMapper.writeValueAsString(q.getOptions())
                                        : null
                        )
                        .build();

                examQuestionRepository.save(examQuestion);
                totalQuestionsSaved++;
            }
        }

        if (totalQuestionsSaved == 0) {
            throw new BadRequestException("AI failed to generate any questions");
        }

        log.info("Exam created successfully. Questions saved: {}", totalQuestionsSaved);

        return exam;
    }

    /* =========================================================
                       GET ALL EXAMS
       ========================================================= */
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    /* =========================================================
                       GET EXAM BY ID
       ========================================================= */
    public Exam getExamById(Long examId) {

        return examRepository.findById(examId)
                .orElseThrow(() ->
                        new BadRequestException("Exam not found with ID: " + examId)
                );
    }

    /* =========================================================
                       UPDATE EXAM
       ========================================================= */
    @Transactional
    public Exam updateExam(Long examId, UpdateExamRequest request) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new BadRequestException("Exam not found with ID: " + examId)
                );

        if (request.getStartDate() != null) {
            exam.setStartDate(request.getStartDate());
        }

        if (request.getStartTime() != null) {
            exam.setStartTime(request.getStartTime());
        }

        if (request.getDurationMinutes() != null) {

            if (request.getDurationMinutes() <= 0) {
                throw new BadRequestException("Duration must be greater than 0");
            }

            exam.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getStatus() != null) {
            exam.setStatus(request.getStatus());
        }

        return examRepository.save(exam);
    }

    /* =========================================================
                       DELETE EXAM (WITH QUESTIONS)
       ========================================================= */
    @Transactional
    public void deleteExam(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new BadRequestException("Exam not found with ID: " + examId)
                );

        examQuestionRepository.deleteByExamId(examId);

        examRepository.delete(exam);

        log.info("Exam deleted successfully: {}", examId);
    }

    /* =========================================================
                       DELETE SINGLE QUESTION
       ========================================================= */
    @Transactional
    public void deleteQuestion(Long questionId) {

        ExamQuestion question = examQuestionRepository.findById(questionId)
                .orElseThrow(() ->
                        new BadRequestException("Question not found with ID: " + questionId)
                );

        examQuestionRepository.delete(question);

        log.info("Question deleted successfully: {}", questionId);
    }

    /* =========================================================
                       GET QUESTIONS BY EXAM ID
       ========================================================= */
    public List<ExamQuestionResponseDTO> getQuestionsByExamId(Long examId) {

        examRepository.findById(examId)
                .orElseThrow(() ->
                        new BadRequestException("Exam not found with ID: " + examId)
                );

        List<ExamQuestion> questions =
                examQuestionRepository.findByExamId(examId);

        return questions.stream()
                .map(q -> {

                    List<String> options = null;

                    try {
                        if (q.getOptionsJson() != null) {
                            options = objectMapper.readValue(
                                    q.getOptionsJson(),
                                    List.class
                            );
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse options JSON", e);
                    }

                    return ExamQuestionResponseDTO.builder()
                            .id(q.getId())
                            .questionType(q.getQuestionType())
                            .question(q.getQuestion())
                            .options(options)
                            .correctAnswer(q.getCorrectAnswer())
                            .difficulty(q.getDifficulty())
                            .topic(q.getTopic())
                            .build();
                })
                .toList();
    }

    /* =========================================================
                       VALIDATION
       ========================================================= */
    private void validateRequest(CreateExamRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Exam title is required");
        }

        if (request.getCourseId() == null) {
            throw new BadRequestException("Course ID is required");
        }

        if (request.getDurationMinutes() == null ||
                request.getDurationMinutes() <= 0) {
            throw new BadRequestException("Duration must be greater than 0");
        }

        MultipartFile document = request.getDocument();

        if (document == null || document.isEmpty()) {
            throw new BadRequestException("Document is required");
        }
    }
}

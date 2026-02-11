package com.example.lms.controller;

import com.example.lms.dto.CreateExamRequest;
import com.example.lms.dto.ExamQuestionResponseDTO;
import com.example.lms.dto.UpdateExamRequest;
import com.example.lms.entity.Exam;
import com.example.lms.service.ExamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Exam> createExam(
            @ModelAttribute CreateExamRequest request) {

        try {
            Exam exam = examService.createExam(request);
            return ResponseEntity.ok(exam);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create exam", e);
        }
    }

    @PutMapping("/{examId}")
    public ResponseEntity<Exam> updateExam(
            @PathVariable Long examId,
            @RequestBody UpdateExamRequest request) {

        Exam updatedExam = examService.updateExam(examId, request);
        return ResponseEntity.ok(updatedExam);
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<ExamQuestionResponseDTO>> getExamQuestions(
            @PathVariable Long examId) {

        return ResponseEntity.ok(
                examService.getQuestionsByExamId(examId)
        );
    }

    /* ================= GET ALL EXAMS ================= */
    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    /* ================= GET EXAM BY ID ================= */
    @GetMapping("/{examId}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.getExamById(examId));
    }

    /* ================= DELETE EXAM ================= */
    @DeleteMapping("/{examId}")
    public ResponseEntity<String> deleteExam(@PathVariable Long examId) {
        examService.deleteExam(examId);
        return ResponseEntity.ok("Exam deleted successfully");
    }

    /* ================= DELETE QUESTION ONLY ================= */
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long questionId) {
        examService.deleteQuestion(questionId);
        return ResponseEntity.ok("Question deleted successfully");
    }



}



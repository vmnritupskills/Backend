package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.service.CodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cm/coding")
@RequiredArgsConstructor
public class CodingCMController {

    private final CodingService service;

    @PostMapping("/questions")
    public ResponseEntity<CodingQuestionResponseDTO> createQuestion(
            @RequestBody CreateCodingQuestionDTO dto
    ) {
        return ResponseEntity.ok(service.createQuestion(dto));
    }

    @GetMapping("/questions")
    public ResponseEntity<List<CodingQuestionResponseDTO>> getAllQuestions() {
        return ResponseEntity.ok(service.getAllQuestions());
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<CodingQuestionResponseDTO> getQuestion(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getQuestion(id));
    }



    @PutMapping("/questions/{id}")
    public ResponseEntity<CodingQuestionResponseDTO> updateQuestion(
            @PathVariable Long id,
            @RequestBody UpdateCodingQuestionDTO dto
    ) {
        return ResponseEntity.ok(service.updateQuestion(id, dto));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        service.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions/{id}/test-cases")
    public ResponseEntity<List<CodingTestCaseResponseDTO>> getAllTestCases(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getAllTestCases(id));
    }




    @PostMapping("/questions/{id}/test-cases")
    public ResponseEntity<CodingTestCaseResponseDTO> addTestCase(
            @PathVariable Long id,
            @RequestBody CreateTestCaseDTO dto
    ) {
        return ResponseEntity.ok(service.addTestCase(id, dto));
    }

    @PutMapping("/test-cases/{id}")
    public ResponseEntity<CodingTestCaseResponseDTO> updateTestCase(
            @PathVariable Long id,
            @RequestBody UpdateTestCaseDTO dto
    ) {
        return ResponseEntity.ok(service.updateTestCase(id, dto));
    }

    @DeleteMapping("/test-cases/{id}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long id) {
        service.deleteTestCase(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/questions/{id}/submissions")
    public ResponseEntity<?> viewSubmissions(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getSubmissions(id));
    }
}


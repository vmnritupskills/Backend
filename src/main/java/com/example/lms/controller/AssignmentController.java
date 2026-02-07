package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cm/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService service;

    /* ================= CREATE ================= */

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<AssignmentResponseDTO> create(
            @RequestParam Long cmId,
            @ModelAttribute @Valid CreateAssignmentDTO dto,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(
                service.createAssignment(cmId, dto, file)
        );
    }

    /* ================= GET ================= */

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<AssignmentResponseDTO>> getByTopic(
            @RequestParam Long cmId,
            @PathVariable Long topicId
    ) {
        return ResponseEntity.ok(
                service.getAssignmentsByTopic(cmId, topicId)
        );
    }

    /* get all */
    @GetMapping
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments(
            @RequestParam Long cmId
    ) {
        return ResponseEntity.ok(
                service.getAllAssignments(cmId)
        );
    }



    /* ================= UPDATE ================= */

    @PutMapping(value = "/{assignmentId}", consumes = "multipart/form-data")
    public ResponseEntity<AssignmentResponseDTO> update(
            @RequestParam Long cmId,
            @PathVariable Long assignmentId,
            @ModelAttribute @Valid CreateAssignmentDTO dto,
            @RequestParam(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(
                service.updateAssignment(cmId, assignmentId, dto, file)
        );
    }

    /* ================= DELETE ================= */

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> delete(
            @RequestParam Long cmId,
            @PathVariable Long assignmentId
    ) {
        service.deleteAssignment(cmId, assignmentId);
        return ResponseEntity.noContent().build();
    }

    /* ================= SUBMISSIONS ================= */

    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponseDTO>> getSubmissions(
            @RequestParam Long cmId,
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok(
                service.getSubmissions(cmId, assignmentId)
        );
    }
}

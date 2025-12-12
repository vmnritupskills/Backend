package com.example.login.controller;

import com.example.login.dto.student.BulkStudentReq;
import com.example.login.dto.student.StudentReq;
import com.example.login.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@PreAuthorize("hasRole('CONTENT_MANAGER')")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody StudentReq req, Principal principal) {
        try {
            return ResponseEntity.ok(studentService.create(req, principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkCreate(@RequestBody BulkStudentReq req, Principal principal) {
        try {
            return ResponseEntity.ok(studentService.bulkCreate(req, principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Long batchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            return ResponseEntity.ok(studentService.list(batchId, page, limit, search));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody StudentReq req,
            Principal principal
    ) {
        try {
            return ResponseEntity.ok(studentService.update(id, req, principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/assign-batch")
    public ResponseEntity<?> assignBatch(
            @PathVariable Long id,
            @RequestParam Long batchId,
            Principal principal
    ) {
        try {
            return ResponseEntity.ok(studentService.assignBatch(id, batchId, principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

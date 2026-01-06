package com.example.lms.controller;

import com.example.lms.dto.BulkEnrollStudentsRequestDTO;
import com.example.lms.dto.EnrollStudentRequestDTO;
import com.example.lms.service.EnrollmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/institution/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /* ================= SINGLE ENROLL ================= */
    @Operation(
            summary = "Enroll a student",
            description = "Enroll a single student into a course under an institution"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student enrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Student or Course not found"),
            @ApiResponse(responseCode = "409", description = "Student already enrolled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @PostMapping
    public ResponseEntity<String> enrollStudent(
            @RequestBody @Valid EnrollStudentRequestDTO dto) {

        enrollmentService.enrollStudent(dto);
        return ResponseEntity.ok("Student enrolled successfully");
    }

    /* ================= BULK ENROLL ================= */
    @Operation(
            summary = "Bulk enroll students",
            description = "Enroll multiple students into a course under an institution"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Students enrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid student list"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Student or Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @PostMapping("/bulk")
    public ResponseEntity<String> bulkEnrollStudents(
            @RequestBody @Valid BulkEnrollStudentsRequestDTO dto) {

        enrollmentService.bulkEnrollStudents(dto);
        return ResponseEntity.ok("Students enrolled successfully");
    }
}

package com.example.lms.controller;

import com.example.lms.dto.CreateStudentRequestDTO;
import com.example.lms.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/institution/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /* ================= CREATE STUDENT ================= */
    @Operation(
            summary = "Create Student",
            description = "Institution creates a student under its account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error / Email or RegNo exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Institution not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @PostMapping
    public ResponseEntity<String> createStudent(
            @RequestBody @Valid CreateStudentRequestDTO dto) {

        studentService.createStudent(dto);
        return ResponseEntity.ok("Student created successfully");
    }
}

package com.example.lms.controller;

import com.example.lms.dto.CreateStudentRequestDTO;
import com.example.lms.dto.UpdateStudentRequestDTO;
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
            description = "ADMIN creates a student under its account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error / Email or RegNo exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN allowed"),
            @ApiResponse(responseCode = "404", description = "ADMIN not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> createStudent(
            @RequestBody @Valid CreateStudentRequestDTO dto) {

        studentService.createStudent(dto);
        return ResponseEntity.ok("Student created successfully");
    }

    /* ================= GET ALL STUDENTS ================= */
    @Operation(summary = "Get All Students", description = "ADMIN gets all students")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }


    /* ================= GET STUDENT BY ID ================= */
    @Operation(summary = "Get Student By ID", description = "ADMIN gets student by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }


    /* ================= UPDATE STUDENT ================= */
    @Operation(summary = "Update Student", description = "ADMIN updates student details")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStudent(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStudentRequestDTO dto) {

        studentService.updateStudent(id, dto);
        return ResponseEntity.ok("Student updated successfully");
    }



    /* ================= DELETE STUDENT ================= */
    @Operation(summary = "Delete Student", description = "ADMIN deletes student")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {

        studentService.deleteStudent(id);
        return ResponseEntity.ok("Student deleted successfully");
    }

}

package com.example.lms.controller;

import com.example.lms.dto.CreateCourseRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/institution/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /* ================= CREATE ================= */
    @Operation(
            summary = "Create Course",
            description = "Institution creates a new course"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error / Invalid input"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "409", description = "Course code already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createCourse(
            @ModelAttribute @Valid CreateCourseRequestDTO dto,
            @RequestParam Long institutionId) {

        courseService.createCourse(dto, institutionId);
        return ResponseEntity.ok("Course created successfully");
    }

    /* ================= GET ALL ================= */
    @Operation(
            summary = "Get all Courses",
            description = "Fetch all courses under an institution"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Courses fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses(
            @RequestParam Long institutionId) {

        return ResponseEntity.ok(
                courseService.getAllCourses(institutionId)
        );
    }

    /* ================= GET BY ID ================= */
    @Operation(
            summary = "Get Course by ID",
            description = "Fetch a specific course by ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('INSTITUTION','CONTENT_MANAGER')")
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseById(
            @PathVariable Long courseId,
            @RequestParam Long institutionId) {

        return ResponseEntity.ok(
                courseService.getCourseById(courseId, institutionId)
        );
    }

    /* ================= UPDATE ================= */
    @Operation(
            summary = "Update Course",
            description = "Update course details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @PutMapping(
            value = "/{courseId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> updateCourse(
            @PathVariable Long courseId,
            @ModelAttribute CreateCourseRequestDTO dto,
            @RequestParam Long institutionId) {

        courseService.updateCourse(courseId, dto, institutionId);
        return ResponseEntity.ok("Course updated successfully");
    }

    /* ================= DELETE ================= */
    @Operation(
            summary = "Delete Course",
            description = "Soft delete a course"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('INSTITUTION')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long courseId,
            @RequestParam Long institutionId) {

        courseService.deleteCourse(courseId, institutionId);
        return ResponseEntity.ok("Course deleted successfully");
    }
}

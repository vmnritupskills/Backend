package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.entity.ContentManager;
import com.example.lms.entity.Course;
import com.example.lms.service.ContentManagerService;
import com.example.lms.dto.ContentManagerStatsResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/institution/content-managers")
@RequiredArgsConstructor
public class ContentManagerController {

    private final ContentManagerService contentManagerService;

    /* ================= CREATE ================= */
    @Operation(summary = "Create Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content Manager created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> createContentManager(
            @RequestBody @Valid CreateContentManagerRequestDTO dto) {

        contentManagerService.createContentManager(dto, dto.institutionId());
        return ResponseEntity.ok("Content Manager created successfully");
    }

    /* ================= GET ALL ================= */
    @Operation(summary = "Get all Content Managers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content Managers fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ContentManagerResponseDTO>> getAllContentManagers(
            @RequestParam Long institutionId) {

        return ResponseEntity.ok(
                contentManagerService.getAllContentManagers(institutionId)
        );
    }

    /* ================= GET BY ID ================= */
    @Operation(summary = "Get Content Manager by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content Manager fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Content Manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')") // for student also
    @GetMapping("/{id}")
    public ResponseEntity<ContentManager> getContentManager(
            @PathVariable Long id,
            @RequestParam Long institutionId) {

        return ResponseEntity.ok(
                contentManagerService.getContentManager(id, institutionId)
        );
    }
    /* ================= UPDATE ================= */
    @Operation(summary = "Update Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content Manager updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Content Manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateContentManager(
            @PathVariable Long id,
            @RequestBody @Valid UpdateContentManagerRequestDTO dto) {

        contentManagerService.updateContentManager(id, dto);
        return ResponseEntity.ok("Content Manager updated successfully");
    }

    /* ================= DELETE ================= */
    @Operation(summary = "Delete Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content Manager deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Content Manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContentManager(
            @PathVariable Long id,
            @RequestParam Long institutionId) {

        contentManagerService.deleteContentManager(id, institutionId);
        return ResponseEntity.ok("Content Manager deleted successfully");
    }

    /* ================= ASSIGN SINGLE COURSE ================= */
    @Operation(summary = "Assign course to Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid course"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN allowed"),
            @ApiResponse(responseCode = "404", description = "Course or Content Manager not found"),
            @ApiResponse(responseCode = "409", description = "Course already assigned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{contentManagerId}/courses/assign")
    public ResponseEntity<String> assignCourse(
            @PathVariable Long contentManagerId,
            @RequestBody @Valid AssignCoursesToContentManagerDTO dto) {

        contentManagerService.assignCourseToContentManager(
                dto.courseId(),
                contentManagerId
        );
        return ResponseEntity.ok("Course assigned successfully");
    }

    /* ================= ASSIGN BULK COURSES ================= */
    @Operation(summary = "Bulk assign courses to Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Courses assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid course list"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN allowed"),
            @ApiResponse(responseCode = "404", description = "Content Manager or Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{contentManagerId}/courses/assign/bulk")
    public ResponseEntity<String> bulkAssignCourses(
            @PathVariable Long contentManagerId,
            @RequestBody @Valid BulkAssignCoursesToContentManagerDTO dto) {

        contentManagerService.bulkAssignCoursesToContentManager(
                dto.courseIds(),
                contentManagerId,
                dto.institutionId()
        );
        return ResponseEntity.ok("Courses assigned successfully");
    }

    /* ================= UNASSIGN COURSE ================= */
    @Operation(summary = "Unassign course from Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Course unassigned successfully"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{contentManagerId}/courses/{courseId}")
    public ResponseEntity<String> unassignCourse(
            @PathVariable Long contentManagerId,
            @PathVariable Long courseId) {

        contentManagerService.unassignCourseFromContentManager(
                courseId,
                contentManagerId
        );
        return ResponseEntity.ok("Course unassigned successfully");
    }

    /* ================= GET ASSIGNED COURSES ================= */
    @Operation(summary = "Get courses assigned to Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned courses fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Content Manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    @GetMapping("/{contentManagerId}/courses")
    public ResponseEntity<List<Course>> getAssignedCourses(
            @PathVariable Long contentManagerId) {

        return ResponseEntity.ok(
                contentManagerService.getCoursesAssignedToContentManager(contentManagerId)
        );
    }

    /* ================= UPDATE ASSIGNED COURSES ================= */
    @Operation(summary = "Replace assigned courses for Content Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned courses updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid course list"),
            @ApiResponse(responseCode = "403", description = "Only Institution allowed"),
            @ApiResponse(responseCode = "404", description = "Content Manager or Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{contentManagerId}/courses")
    public ResponseEntity<String> updateAssignedCourses(
            @PathVariable Long contentManagerId,
            @RequestBody @Valid UpdateAssignedCoursesDTO dto) {

        contentManagerService.updateAssignedCourses(
                dto.courseIds(),
                contentManagerId
        );
        return ResponseEntity.ok("Assigned courses updated successfully");
    }

    /* ================= ADMIN STATS ================= */
    @Operation(summary = "Get Content Manager statistics (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/stats")
    public ResponseEntity<ContentManagerStatsResponseDTO> getContentManagerStats() {

        return ResponseEntity.ok(
                contentManagerService.getContentManagerStats()
        );
    }

}

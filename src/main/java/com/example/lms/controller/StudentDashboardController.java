package com.example.lms.controller;

import com.example.lms.dto.StudentCourseResponseDTO;
import com.example.lms.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    @GetMapping("/me/courses")
    public ResponseEntity<StudentCourseResponseDTO> getMyCourses(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                studentDashboardService.getMyCourses(userId)
        );
    }
}

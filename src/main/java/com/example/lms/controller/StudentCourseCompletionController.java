package com.example.lms.controller;

import com.example.lms.dto.CourseCompletionDTO;
import com.example.lms.service.StudentCourseCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentCourseCompletionController {

    private final StudentCourseCompletionService service;

    @GetMapping("/course-completion/{courseId}")
    public ResponseEntity<CourseCompletionDTO> getCourseCompletion(
            @PathVariable Long courseId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                service.getCourseCompletion(userId, courseId)
        );
    }
}

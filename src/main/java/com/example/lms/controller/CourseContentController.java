package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.entity.CourseSubtopic;
import com.example.lms.entity.CourseTopic;
import com.example.lms.security.UserPrincipal;
import com.example.lms.service.CourseContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cm/course-content")
@RequiredArgsConstructor
public class CourseContentController {

    private final CourseContentService service;

    /* ================= TOPIC ================= */

    @PostMapping("/topics")
    public ResponseEntity<CourseTopic> createTopic(
            @RequestParam Long cmId,
            @RequestBody @Valid CreateTopicDTO dto
    ) {
        return ResponseEntity.ok(
                service.createTopic(cmId, dto)
        );
    }

    @GetMapping("/topics/{courseId}")
    public ResponseEntity<List<CourseTopic>> getTopics(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                service.getTopicsByCourse(courseId)
        );
    }

    @PutMapping("/topics/{topicId}")
    public ResponseEntity<CourseTopic> updateTopic(
            @PathVariable Long topicId,
            @RequestBody @Valid UpdateTopicDTO dto
    ) {
        return ResponseEntity.ok(
                service.updateTopic(topicId, dto)
        );
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(
            @RequestParam Long cmId,
            @PathVariable Long topicId
    ) {
        service.deleteTopic(cmId, topicId);
        return ResponseEntity.noContent().build();
    }

    /* ================= SUBTOPIC ================= */

    @PostMapping(value = "/subtopics", consumes = "multipart/form-data")
    public ResponseEntity<CourseSubtopic> createSubtopic(
            @RequestParam Long cmId,
            @ModelAttribute @Valid CreateSubtopicDTO dto,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(
                service.createSubtopic(cmId, dto, file)
        );
    }



    @GetMapping("/subtopics/{topicId}")
    public ResponseEntity<List<CourseSubtopic>> getSubtopics(
            @PathVariable Long topicId
    ) {
        return ResponseEntity.ok(
                service.getSubtopicsByTopic(topicId)
        );
    }

    @DeleteMapping("/subtopics/{subtopicId}")
    public ResponseEntity<Void> deleteSubtopic(
            @PathVariable Long subtopicId
    ) {
        service.deleteSubtopic(subtopicId);
        return ResponseEntity.noContent().build();
    }

    /* ================= COURSE STRUCTURE ================= */

    @GetMapping("/course/{courseId}/structure")
    public ResponseEntity<CourseContentResponseDTO> getCourseStructure(
            @RequestParam Long cmId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                service.getCourseContentStructure(cmId, courseId)
        );
    }

    @GetMapping("/course/{courseId}/completion")
    public ResponseEntity<CourseCompletionStatsDTO> getCourseCompletionStats(
            @RequestParam Long cmId,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                service.getAverageCourseCompletion(cmId, courseId)
        );
    }

}


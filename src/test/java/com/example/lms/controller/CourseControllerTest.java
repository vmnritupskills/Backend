package com.example.lms.controller;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.dto.CreateCourseRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CourseController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void createCourse_success() throws Exception {
        doNothing().when(courseService).createCourse(any(CreateCourseRequestDTO.class), eq(1L));

        // Note: Field names must match CreateCourseRequestDTO exactly
        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("name", "Java Programming") // Matches 'name' in DTO
                        .param("courseCode", "JAVA101")
                        .param("duration", "4 Months")
                        .param("semester", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Course created successfully"));
    }

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void createCourse_validation_failure() throws Exception {
        // Missing 'name' and 'duration' to trigger MethodArgumentNotValidException
        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("courseCode", "JAVA101")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void getAllCourses_success() throws Exception {
        // Use the builder instead of "new Course()"
        Course course = Course.builder()
                .id(101L)
                .name("Spring Boot")
                .build();

        when(courseService.getAllCourses(1L)).thenReturn(List.of(course));

        mockMvc.perform(get("/api/institution/courses")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spring Boot")); // Note: field is 'name', not 'courseName'
    }

    /* ================= GET BY ID ================= */

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void getCourseById_success() throws Exception {
        // Use the Builder to bypass the protected constructor
        Course course = Course.builder()
                .id(101L)
                .name("Java Programming") // Entity uses 'name'
                .courseCode("JAVA101")
                .build();

        when(courseService.getCourseById(101L, 1L)).thenReturn(course);

        mockMvc.perform(get("/api/institution/courses/101")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.name").value("Java Programming")); // Updated from courseName to name
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void updateCourse_success() throws Exception {
        doNothing().when(courseService).updateCourse(eq(101L), any(CreateCourseRequestDTO.class), eq(1L));

        mockMvc.perform(multipart("/api/institution/courses/101")
                        .param("institutionId", "1")
                        .param("name", "Advanced Java")
                        .param("courseCode", "JAVA102")
                        .param("duration", "6 Months")
                        .param("semester", "2")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().string("Course updated successfully"));
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void deleteCourse_success() throws Exception {
        doNothing().when(courseService).deleteCourse(101L, 1L);

        mockMvc.perform(delete("/api/institution/courses/101")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Course deleted successfully"));
    }

    /* ================= SECURITY ================= */

    @Test
    @WithMockUser(roles = "USER") // ROLE_USER should be rejected with 403
    void createCourse_forbidden_for_user() throws Exception {
        // 1. Use multipart() instead of post()
        // 2. Set the contentType explicitly to satisfy the controller
        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("name", "Test Course")
                        .param("courseCode", "T101")
                        .param("duration", "1 month")
                        .param("semester", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }
}
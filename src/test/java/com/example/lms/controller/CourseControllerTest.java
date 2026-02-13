package com.example.lms.controller;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.controller.CourseController;
import com.example.lms.dto.CourseResponseDTO;
import com.example.lms.dto.CreateCourseRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.service.CourseService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(controllers = CourseController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("CourseController Tests")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean

    private CourseService courseService;

    @MockBean

    private JwtUtil jwtUtil;

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_success() throws Exception {
        doNothing().when(courseService).createCourse(any(), eq(1L));

        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("name", "Java Programming")
                        .param("courseCode", "JAVA101")
                        .param("duration", "4 Months")
                        .param("semester", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Course created successfully"));

        verify(courseService).createCourse(any(CreateCourseRequestDTO.class), eq(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_missingName_400() throws Exception {
        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("courseCode", "JAVA101")
                        .param("duration", "4 Months")
                        .param("semester", "1"))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).createCourse(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_duplicateCode_400() throws Exception {
        doThrow(new IllegalArgumentException("Course code already exists"))
                .when(courseService).createCourse(any(), eq(1L));

        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("name", "Java Programming")
                        .param("courseCode", "JAVA101")
                        .param("duration", "4 Months")
                        .param("semester", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void createCourse_forbidden() throws Exception {
        mockMvc.perform(multipart("/api/institution/courses")
                        .param("institutionId", "1")
                        .param("name", "Java Programming")
                        .param("courseCode", "JAVA101")
                        .param("duration", "4 Months")
                        .param("semester", "1"))
                .andExpect(status().isForbidden());
    }

    /* ================= GET ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCourses_success() throws Exception {

        CourseResponseDTO dto = new CourseResponseDTO(
                1L,
                "Java Programming",
                "JAVA101",
                "4",
                1,
                null,
                1L,
                "ABC Institution"
        );

        when(courseService.getAllCourses(1L))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/institution/courses")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java Programming"))
                .andExpect(jsonPath("$[0].courseCode").value("JAVA101"));
    }


    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void getAllCourses_forbidden() throws Exception {
        mockMvc.perform(get("/api/institution/courses")
                        .param("institutionId", "1"))
                .andExpect(status().isForbidden());
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourse_success() throws Exception {
        doNothing().when(courseService).updateCourse(eq(1L), any(), eq(1L));

        mockMvc.perform(multipart("/api/institution/courses/1")
                        .param("institutionId", "1")
                        .param("name", "Advanced Java")
                        .param("courseCode", "JAVA102")
                        .param("duration", "6 Months")
                        .param("semester", "2")
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("Course updated successfully"));

        verify(courseService).updateCourse(eq(1L), any(CreateCourseRequestDTO.class), eq(1L));
    }




    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCourse_notFound_404() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found"))
                .when(courseService).updateCourse(eq(999L), any(), eq(1L));

        mockMvc.perform(multipart("/api/institution/courses/999")
                        .param("institutionId", "1")
                        .param("name", "Java")
                        .param("courseCode", "JAVA999")
                        .param("duration", "6 Months")
                        .param("semester", "2")
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isNotFound());
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourse_success() throws Exception {
        doNothing().when(courseService).deleteCourse(1L, 1L);

        mockMvc.perform(delete("/api/institution/courses/1")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Course deleted successfully"));

        verify(courseService).deleteCourse(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteCourse_forbidden() throws Exception {
        mockMvc.perform(delete("/api/institution/courses/1")
                        .param("institutionId", "1"))
                .andExpect(status().isForbidden());
    }
}

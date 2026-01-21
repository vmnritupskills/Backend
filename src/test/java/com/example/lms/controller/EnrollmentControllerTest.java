package com.example.lms.controller;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.dto.BulkEnrollStudentsRequestDTO;
import com.example.lms.dto.EnrollStudentRequestDTO;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.service.EnrollmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EnrollmentController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    /* ================= SINGLE ENROLL ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void enrollStudent_success() throws Exception {
        EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STUDENT001", 101L);

        doNothing().when(enrollmentService).enrollStudent(any(EnrollStudentRequestDTO.class));

        mockMvc.perform(post("/api/institution/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Student enrolled successfully"));
    }

    /* ================= BULK ENROLL ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void bulkEnrollStudents_success() throws Exception {
        BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(
                1L,
                101L,
                List.of("STUDENT001", "STUDENT002")
        );

        doNothing().when(enrollmentService).bulkEnrollStudents(any(BulkEnrollStudentsRequestDTO.class));

        mockMvc.perform(post("/api/institution/enrollments/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Students enrolled successfully"));
    }

    /* ================= SECURITY CHECK ================= */

    @Test
    @WithMockUser(roles = "USER")
    void enrollStudent_forbidden_for_user() throws Exception {
        EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STUDENT001", 101L);

        mockMvc.perform(post("/api/institution/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
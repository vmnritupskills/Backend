package com.example.lms.controller;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.dto.CreateStudentRequestDTO;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({StudentController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void createStudent_success() throws Exception {
        CreateStudentRequestDTO dto = new CreateStudentRequestDTO(
                1L,
                "REG12345",
                "Alice Smith",
                "alice@student.com",
                "Password123",
                2024,
                "Computer Science"
        );

        doNothing().when(studentService).createStudent(any(CreateStudentRequestDTO.class));

        mockMvc.perform(post("/api/institution/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Student created successfully"));
    }
}
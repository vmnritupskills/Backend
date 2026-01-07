package com.example.lms;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.controller.AuthController;
import com.example.lms.dto.LoginRequestDTO;
import com.example.lms.dto.LoginResponseDTO;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, SecurityConfig.class})

@Import({TestJwtKeyConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void login_success() throws Exception {

        LoginRequestDTO request = new LoginRequestDTO("admin@lms.com", "111222");
        LoginResponseDTO response = new LoginResponseDTO(
                "mock-jwt-token", 1L, "adminUser", "admin@lms.com", "ROLE_ADMIN"
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void login_fail_invalid_input() throws Exception {

        LoginRequestDTO invalidRequest = new LoginRequestDTO("admin@lms.com", "");


        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }
}

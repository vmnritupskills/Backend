package com.example.lms.controller;

import com.example.lms.TestJwtKeyConfig;
import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.dto.LoginRequestDTO;
import com.example.lms.dto.LoginResponseDTO;
import com.example.lms.exception.ForbiddenException;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.UnauthorizedException;
import com.example.lms.service.AuthService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class, SecurityConfig.class})
@Import({TestJwtKeyConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login with valid credentials should return 200 with token and user details")
    void testLoginSuccess() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@lms.com", "password123");
        LoginResponseDTO response = new LoginResponseDTO(
                "mock-jwt-token-12345",
                1L,
                "adminUser",
                "admin@lms.com",
                "ROLE_ADMIN"
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-12345"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("adminUser"))
                .andExpect(jsonPath("$.email").value("admin@lms.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Login with student role should return correct role")
    void testLoginSuccessStudentRole() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("student@lms.com", "password123");
        LoginResponseDTO response = new LoginResponseDTO(
                "student-token",
                2L,
                "studentUser",
                "student@lms.com",
                "ROLE_STUDENT"
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_STUDENT"));
    }

    @Test
    @DisplayName("Login with empty email should return 400 Bad Request")
    void testLoginFailEmptyEmail() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO("", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Login with empty password should return 400 Bad Request")
    void testLoginFailEmptyPassword() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO("admin@lms.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Login with invalid email format should return 400 Bad Request")
    void testLoginFailInvalidEmailFormat() throws Exception {
        LoginRequestDTO invalidRequest = new LoginRequestDTO("invalid-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Login with null email should return 400 Bad Request")
    void testLoginFailNullEmail() throws Exception {
        String jsonPayload = "{\"email\": null, \"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Login with null password should return 400 Bad Request")
    void testLoginFailNullPassword() throws Exception {
        String jsonPayload = "{\"email\": \"admin@lms.com\", \"password\": null}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("Login with invalid credentials should return 401 Unauthorized")
    void testLoginFailUnauthorized() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@lms.com", "wrongpassword");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with non-existent user should return 404 Not Found")
    void testLoginFailUserNotFound() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("nonexistent@lms.com", "password123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Login with disabled account should return 403 Forbidden")
    void testLoginFailAccountDisabled() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("disabled@lms.com", "password123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new ForbiddenException("User account is disabled or locked"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login with malformed JSON should return 400 Bad Request")
    void testLoginFailMalformedJson() throws Exception {
        String malformedJson = "{invalid json}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }


    @Test
    @DisplayName("Login without Content-Type should return 415 Unsupported Media Type")
    void testLoginWithoutContentType() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@lms.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        verify(authService, never()).login(any());
    }


    // ==================== LOGOUT TESTS ====================

    @Test
    @WithMockUser
    @DisplayName("Logout with valid token should return 200 with success message")
    void testLogoutSuccess() throws Exception {
        String token = "valid-jwt-token";

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout(token);
    }

    @Test
    @WithMockUser
    @DisplayName("Logout without Authorization header should return 200")
    void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService, never()).logout(any(String.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Logout with Bearer token should extract and use token")
    void testLogoutWithBearerToken() throws Exception {
        String token = "my-secret-token-xyz";

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout(token);
    }

    @Test
    @WithMockUser
    @DisplayName("Logout with invalid Authorization format should return 200")
    void testLogoutWithInvalidAuthFormat() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService, never()).logout(any(String.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Logout with Basic auth should not extract token")
    void testLogoutWithBasicAuth() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isOk());

        verify(authService, never()).logout(any(String.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Logout with Bearer prefix but no token should return 200")
    void testLogoutWithBearerPrefixOnly() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout("");
    }

    @Test
    @WithMockUser
    @DisplayName("Logout response should contain message field")
    void testLogoutResponseStructure() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("Logged out successfully")));
    }
}

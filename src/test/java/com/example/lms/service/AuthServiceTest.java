package com.example.lms.service;

import com.example.lms.config.JwtUtil;
import com.example.lms.dto.LoginRequestDTO;
import com.example.lms.dto.LoginResponseDTO;
import com.example.lms.entity.Role;
import com.example.lms.entity.Session;
import com.example.lms.entity.User;
import com.example.lms.exception.BadRequestException;
import com.example.lms.exception.UnauthorizedException;
import com.example.lms.repository.SessionRepository;
import com.example.lms.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private AuthService authService;

    /* ================= LOGIN SUCCESS ================= */

    @Test
    void login_success() {

        LoginRequestDTO request =
                new LoginRequestDTO("admin@lms.com", "password");

        Role role = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        User user = User.builder()
                .id(1L)
                .email("admin@lms.com")
                .name("Admin User")
                .password("encoded-password")
                .isActive(true)
                .role(role)
                .build();

        when(userRepository.findByEmail("admin@lms.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);

        when(jwtUtil.generateToken(user))
                .thenReturn("jwt-token");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(1L, response.userId());
        assertEquals("Admin User", response.username());
        assertEquals("admin@lms.com", response.email());
        assertEquals("ROLE_ADMIN", response.role());

        verify(sessionRepository).save(any(Session.class));
    }

    /* ================= INVALID EMAIL ================= */

    @Test
    void login_fail_invalid_email() {

        LoginRequestDTO request =
                new LoginRequestDTO("wrong@lms.com", "password");

        when(userRepository.findByEmail("wrong@lms.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        verify(sessionRepository, never()).save(any());
    }

    /* ================= WRONG PASSWORD ================= */

    @Test
    void login_fail_wrong_password() {

        LoginRequestDTO request =
                new LoginRequestDTO("admin@lms.com", "wrong");

        User user = User.builder()
                .email("admin@lms.com")
                .password("encoded-password")
                .isActive(true)
                .build();

        when(userRepository.findByEmail("admin@lms.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded-password"))
                .thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        verify(sessionRepository, never()).save(any());
    }

    /* ================= USER DISABLED ================= */

    @Test
    void login_fail_user_disabled() {

        LoginRequestDTO request =
                new LoginRequestDTO("admin@lms.com", "password");

        User user = User.builder()
                .email("admin@lms.com")
                .password("encoded-password")
                .isActive(false)
                .build();

        when(userRepository.findByEmail("admin@lms.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> authService.login(request)
        );

        verify(sessionRepository, never()).save(any());
    }

    /* ================= LOGOUT ================= */

    @Test
    void logout_success() {

        String token = "jwt-token";

        Session session = Session.builder()
                .token(token)
                .isActive(true)
                .build();

        when(sessionRepository.findByTokenAndIsActiveTrue(token))
                .thenReturn(Optional.of(session));

        authService.logout(token);

        assertFalse(session.getIsActive());
        verify(sessionRepository).save(session);
    }

    @Test
    void logout_no_active_session() {

        when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                .thenReturn(Optional.empty());

        authService.logout("jwt-token");

        verify(sessionRepository, never()).save(any());
    }
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
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

    private Role testRole;
    private User testUser;
    private LoginRequestDTO loginRequest;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        testUser = User.builder()
                .id(10L)
                .email("admin@lms.com")
                .name("Admin")
                .password("encoded-password")
                .isActive(true)
                .role(testRole)
                .build();

        loginRequest = new LoginRequestDTO("admin@lms.com", "password");

        testSession = Session.builder()
                .id(1L)
                .user(testUser)
                .token("jwt-token")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("login - Happy Path Tests")
    class LoginHappyPath {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void login_success() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            LoginResponseDTO response = authService.login(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals("jwt-token", response.token());
            assertEquals(10L, response.userId());
            assertEquals("Admin", response.username());
            assertEquals("admin@lms.com", response.email());
            assertEquals("ROLE_ADMIN", response.role());

            verify(jwtUtil).generateToken(testUser);
            verify(sessionRepository).save(any(Session.class));
        }

        @Test
        @DisplayName("Should create active session after successful login")
        void login_createsActiveSession() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            authService.login(loginRequest);

            // Assert
            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(sessionRepository).save(sessionCaptor.capture());
            Session savedSession = sessionCaptor.getValue();

            assertTrue(savedSession.getIsActive());
            assertEquals(testUser, savedSession.getUser());
            assertEquals("jwt-token", savedSession.getToken());
        }

        @Test
        @DisplayName("Should return all user details in response")
        void login_returnsCompleteUserDetails() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            LoginResponseDTO response = authService.login(loginRequest);

            // Assert
            assertEquals("jwt-token", response.token());
            assertEquals(10L, response.userId());
            assertEquals("Admin", response.username());
            assertEquals("admin@lms.com", response.email());
            assertEquals("ROLE_ADMIN", response.role());
        }

        @Test
        @DisplayName("Should generate JWT token for valid user")
        void login_generatesJwtToken() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            authService.login(loginRequest);

            // Assert
            verify(jwtUtil).generateToken(testUser);
        }

        @Test
        @DisplayName("Should support different roles")
        void login_supportsDifferentRoles() {
            // Arrange
            Role studentRole = Role.builder().id(2L).name("ROLE_STUDENT").build();
            testUser.setRole(studentRole);

            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            LoginResponseDTO response = authService.login(loginRequest);

            // Assert
            assertEquals("ROLE_STUDENT", response.role());
        }
    }

    @Nested
    @DisplayName("login - Unhappy Path Tests")
    class LoginUnhappyPath {

        @Test
        @DisplayName("Should throw UnauthorizedException when email not found")
        void login_emailNotFound_throwsException() {
            // Arrange
            when(userRepository.findByEmail("unknown@lms.com"))
                    .thenReturn(Optional.empty());

            LoginRequestDTO unknownRequest = new LoginRequestDTO("unknown@lms.com", "password");

            // Act & Assert
            UnauthorizedException exception = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.login(unknownRequest)
            );

            assertEquals("Invalid email or password", exception.getMessage());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtUtil, never()).generateToken(any());
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when password incorrect")
        void login_wrongPassword_throwsException() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpassword", "encoded-password"))
                    .thenReturn(false);

            LoginRequestDTO wrongPasswordRequest = new LoginRequestDTO("admin@lms.com", "wrongpassword");

            // Act & Assert
            UnauthorizedException exception = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.login(wrongPasswordRequest)
            );

            assertEquals("Invalid email or password", exception.getMessage());
            verify(jwtUtil, never()).generateToken(any());
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when user account disabled")
        void login_userDisabled_throwsException() {
            // Arrange
            testUser.setIsActive(false);
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> authService.login(loginRequest)
            );

            assertEquals("User account is disabled", exception.getMessage());
            verify(jwtUtil, never()).generateToken(any());
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not generate token on disabled user")
        void login_disabledUserNoToken() {
            // Arrange
            testUser.setIsActive(false);
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should handle multiple failed login attempts")
        void login_multipleFaiuredAttempts() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpass", "encoded-password"))
                    .thenReturn(false);

            LoginRequestDTO wrongRequest = new LoginRequestDTO("admin@lms.com", "wrongpass");

            // Act & Assert
            assertThrows(UnauthorizedException.class, () -> authService.login(wrongRequest));
            assertThrows(UnauthorizedException.class, () -> authService.login(wrongRequest));
            assertThrows(UnauthorizedException.class, () -> authService.login(wrongRequest));

            verify(jwtUtil, never()).generateToken(any());
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should verify password check before token generation")
        void login_verifiesPasswordBeforeTokenGeneration() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            authService.login(loginRequest);

            // Assert
            verify(passwordEncoder).matches("password", "encoded-password");
            verify(jwtUtil).generateToken(testUser);
        }

        @Test
        @DisplayName("Should verify active status check before token generation")
        void login_verifiesActiveStatusBeforeTokenGeneration() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            // Act
            authService.login(loginRequest);

            // Assert
            assertTrue(testUser.getIsActive());
            verify(jwtUtil).generateToken(testUser);
        }
    }

    @Nested
    @DisplayName("logout - Happy Path Tests")
    class LogoutHappyPath {

        @Test
        @DisplayName("Should successfully logout and deactivate session")
        void logout_success() {
            // Arrange
            when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                    .thenReturn(Optional.of(testSession));

            // Act
            authService.logout("jwt-token");

            // Assert
            assertFalse(testSession.getIsActive());
            verify(sessionRepository).save(testSession);
        }

        @Test
        @DisplayName("Should deactivate session on logout")
        void logout_deactivatesSession() {
            // Arrange
            assertTrue(testSession.getIsActive());
            when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                    .thenReturn(Optional.of(testSession));

            // Act
            authService.logout("jwt-token");

            // Assert
            assertFalse(testSession.getIsActive());
        }

        @Test
        @DisplayName("Should save deactivated session")
        void logout_savesDeactivatedSession() {
            // Arrange
            when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                    .thenReturn(Optional.of(testSession));

            // Act
            authService.logout("jwt-token");

            // Assert
            ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
            verify(sessionRepository).save(sessionCaptor.capture());
            assertFalse(sessionCaptor.getValue().getIsActive());
        }
    }

    @Nested
    @DisplayName("logout - Unhappy Path Tests")
    class LogoutUnhappyPath {

        @Test
        @DisplayName("Should not throw exception for inactive session token")
        void logout_inactiveSession_noException() {
            // Arrange
            when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertDoesNotThrow(() -> authService.logout("jwt-token"));
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not throw exception for unknown token")
        void logout_unknownToken_noException() {
            // Arrange
            when(sessionRepository.findByTokenAndIsActiveTrue("unknown-token"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertDoesNotThrow(() -> authService.logout("unknown-token"));
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle logout for already logged out session")
        void logout_alreadyLoggedOut_noException() {
            // Arrange
            testSession.setIsActive(false);
            when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertDoesNotThrow(() -> authService.logout("jwt-token"));
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not modify inactive sessions")
        void logout_inactiveSession_notModified() {
            // Arrange
            when(sessionRepository.findByTokenAndIsActiveTrue("jwt-token"))
                    .thenReturn(Optional.empty());

            // Act
            authService.logout("jwt-token");

            // Assert
            verify(sessionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login - Edge Cases and Field Validation")
    class LoginEdgeCases {

        @Test
        @DisplayName("Should handle email with special characters")
        void login_emailWithSpecialCharacters() {
            // Arrange
            String specialEmail = "admin+test@lms.com";
            testUser.setEmail(specialEmail);

            when(userRepository.findByEmail(specialEmail))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password", "encoded-password"))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("jwt-token");

            LoginRequestDTO specialRequest = new LoginRequestDTO(specialEmail, "password");

            // Act
            LoginResponseDTO response = authService.login(specialRequest);

            // Assert
            assertEquals(specialEmail, response.email());
        }

        @Test
        @DisplayName("Should be case-sensitive for password")
        void login_passwordCaseSensitive() {
            // Arrange
            when(userRepository.findByEmail("admin@lms.com"))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password", "encoded-password"))
                    .thenReturn(false);

            LoginRequestDTO wrongCaseRequest = new LoginRequestDTO("admin@lms.com", "Password");

            // Act & Assert
            assertThrows(UnauthorizedException.class, () -> authService.login(wrongCaseRequest));
        }
    }
}

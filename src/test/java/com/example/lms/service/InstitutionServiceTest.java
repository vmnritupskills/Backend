package com.example.lms.service;

import com.example.lms.dto.CreateInstitutionRequestDTO;
import com.example.lms.dto.InstitutionResponseDTO;
import com.example.lms.dto.UpdateInstitutionRequestDTO;
import com.example.lms.entity.Institution;
import com.example.lms.entity.Role;
import com.example.lms.entity.User;
import com.example.lms.exception.BadRequestException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.InstitutionRepository;
import com.example.lms.repository.RoleRepository;
import com.example.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;


import org.mockito.ArgumentCaptor;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(MockitoExtension.class)
@DisplayName("InstitutionService Tests")
class InstitutionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InstitutionService institutionService;

    private Institution testInstitution;
    private User testUser;
    private Role testRole;
    private CreateInstitutionRequestDTO createDTO;
    private UpdateInstitutionRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("INSTITUTION")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("institution@example.com")
                .name("Test Institution")
                .password("encodedPassword")
                .role(testRole)
                .isActive(true)
                .build();

        testInstitution = Institution.builder()
                .id(1L)
                .name("Test Institution")
                .address("Test Address")
                .aisheCode("TEST001")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        createDTO = new CreateInstitutionRequestDTO(
                "Test Institution",
                "institution@example.com",
                "password123",
                "Test Address",
                "TEST001"
        );

        updateDTO = new UpdateInstitutionRequestDTO(
                "Updated Institution",
                "Updated Address",
                "TEST002",
                true
        );
    }

    @Nested
    @DisplayName("createInstitution - Happy Path Tests")
    class CreateInstitutionHappyPath {

        @Test
        @DisplayName("Should successfully create a new institution")
        void createInstitution_success() {
            // Arrange
            when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
            when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(createDTO.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(1L);
                return u;
            });

            // Act
            institutionService.createInstitution(createDTO);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertEquals("institution@example.com", savedUser.getEmail());
            assertEquals("Test Institution", savedUser.getName());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertTrue(savedUser.getIsActive());

            verify(institutionRepository).save(any(Institution.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void createInstitution_encodesPassword() {
            // Arrange
            when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
            when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

            // Act
            institutionService.createInstitution(createDTO);

            // Assert
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should set createdAt timestamp")
        void createInstitution_setsCreatedAt() {
            // Arrange
            when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
            when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(createDTO.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            institutionService.createInstitution(createDTO);

            // Assert
            ArgumentCaptor<Institution> institutionCaptor = ArgumentCaptor.forClass(Institution.class);
            verify(institutionRepository).save(institutionCaptor.capture());
            Institution savedInstitution = institutionCaptor.getValue();

            assertNotNull(savedInstitution.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("createInstitution - Unhappy Path Tests")
    class CreateInstitutionUnhappyPath {

        @Test
        @DisplayName("Should throw BadRequestException when email already exists")
        void createInstitution_emailExists_throwsException() {
            // Arrange
            when(userRepository.findByEmail(createDTO.email()))
                    .thenReturn(Optional.of(testUser));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> institutionService.createInstitution(createDTO)
            );

            assertEquals("Email already exists", exception.getMessage());
            verify(roleRepository, never()).findByName(anyString());
            verify(institutionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when role not found")
        void createInstitution_roleNotFound_throwsException() {
            // Arrange
            when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
            when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.createInstitution(createDTO)
            );

            assertEquals("Role not found", exception.getMessage());
            verify(userRepository, never()).save(any());
            verify(institutionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not save institution if user save fails")
        void createInstitution_userSaveFails_doesNotSaveInstitution() {
            // Arrange
            when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
            when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode(createDTO.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> institutionService.createInstitution(createDTO));
            verify(institutionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllInstitutions - Happy Path Tests")
    class GetAllInstitutionsHappyPath {

        @Test
        @DisplayName("Should retrieve all institutions successfully")
        void getAllInstitutions_success() {
            // Arrange
            Institution inst2 = Institution.builder()
                    .id(2L)
                    .name("Institution 2")
                    .address("Address 2")
                    .aisheCode("TEST002")
                    .user(User.builder().id(2L).isActive(true).build())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(institutionRepository.findByDeletedAtIsNull())
                    .thenReturn(List.of(testInstitution, inst2));

            // Act
            List<InstitutionResponseDTO> result = institutionService.getAllInstitutions();

            // Assert
            assertEquals(2, result.size());
            assertEquals("Test Institution", result.get(0).name());
            assertEquals("Institution 2", result.get(1).name());
        }

        @Test
        @DisplayName("Should return empty list when no institutions exist")
        void getAllInstitutions_empty_returnsEmptyList() {
            // Arrange
            when(institutionRepository.findByDeletedAtIsNull())
                    .thenReturn(new ArrayList<>());

            // Act
            List<InstitutionResponseDTO> result = institutionService.getAllInstitutions();

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should map all fields correctly")
        void getAllInstitutions_mapsFieldsCorrectly() {
            // Arrange
            when(institutionRepository.findByDeletedAtIsNull())
                    .thenReturn(List.of(testInstitution));

            // Act
            List<InstitutionResponseDTO> result = institutionService.getAllInstitutions();

            // Assert
            InstitutionResponseDTO dto = result.get(0);
            assertEquals(testInstitution.getId(), dto.id());
            assertEquals(testInstitution.getName(), dto.name());
            assertEquals(testInstitution.getAddress(), dto.address());
            assertEquals(testInstitution.getAisheCode(), dto.aisheCode());
            assertEquals(testInstitution.getUser().getId(), dto.userId());
            assertTrue(dto.isActive());
        }
    }

    @Nested
    @DisplayName("getInstitutionById - Happy Path Tests")
    class GetInstitutionByIdHappyPath {

        @Test
        @DisplayName("Should retrieve institution by ID successfully")
        void getInstitutionById_success() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            InstitutionResponseDTO result = institutionService.getInstitutionById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("Test Institution", result.name());
            assertEquals("Test Address", result.address());
            assertEquals("TEST001", result.aisheCode());
            assertEquals(1L, result.userId());
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("Should include createdAt timestamp in response")
        void getInstitutionById_includesTimestamp() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            testInstitution.setCreatedAt(now);
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            InstitutionResponseDTO result = institutionService.getInstitutionById(1L);

            // Assert
            assertEquals(now, result.createdAt());
        }
    }

    @Nested
    @DisplayName("getInstitutionById - Unhappy Path Tests")
    class GetInstitutionByIdUnhappyPath {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when institution not found")
        void getInstitutionById_notFound_throwsException() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.getInstitutionById(999L)
            );

            assertEquals("Institution not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should not return soft-deleted institutions")
        void getInstitutionById_softDeleted_throwsException() {
            // Arrange
            testInstitution.setDeletedAt(LocalDateTime.now());
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.getInstitutionById(1L)
            );
        }
    }

    @Nested
    @DisplayName("updateInstitution - Happy Path Tests")
    class UpdateInstitutionHappyPath {

        @Test
        @DisplayName("Should successfully update all fields")
        void updateInstitution_success() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.updateInstitution(1L, updateDTO);

            // Assert
            assertEquals("Updated Institution", testInstitution.getName());
            assertEquals("Updated Address", testInstitution.getAddress());
            assertEquals("TEST002", testInstitution.getAisheCode());
            assertTrue(testInstitution.getUser().getIsActive());

            verify(institutionRepository).save(testInstitution);
        }

        @Test
        @DisplayName("Should handle partial updates - update name only")
        void updateInstitution_partialUpdate_nameOnly() {
            // Arrange
            UpdateInstitutionRequestDTO partialDTO = new UpdateInstitutionRequestDTO(
                    "Updated Name", null, null, null
            );
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.updateInstitution(1L, partialDTO);

            // Assert
            assertEquals("Updated Name", testInstitution.getName());
            assertEquals("Test Address", testInstitution.getAddress()); // unchanged
            assertEquals("TEST001", testInstitution.getAisheCode()); // unchanged
        }

        @Test
        @DisplayName("Should handle partial updates - update address only")
        void updateInstitution_partialUpdate_addressOnly() {
            // Arrange
            UpdateInstitutionRequestDTO partialDTO = new UpdateInstitutionRequestDTO(
                    null, "Updated Address", null, null
            );
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.updateInstitution(1L, partialDTO);

            // Assert
            assertEquals("Test Institution", testInstitution.getName()); // unchanged
            assertEquals("Updated Address", testInstitution.getAddress());
        }

        @Test
        @DisplayName("Should toggle active status")
        void updateInstitution_togglesActiveStatus() {
            // Arrange
            assertTrue(testInstitution.getUser().getIsActive());
            UpdateInstitutionRequestDTO deactivateDTO = new UpdateInstitutionRequestDTO(
                    null, null, null, false
            );
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.updateInstitution(1L, deactivateDTO);

            // Assert
            assertFalse(testInstitution.getUser().getIsActive());
        }
    }

    @Nested
    @DisplayName("updateInstitution - Unhappy Path Tests")
    class UpdateInstitutionUnhappyPath {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when institution not found")
        void updateInstitution_notFound_throwsException() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.updateInstitution(999L, updateDTO)
            );

            assertEquals("Institution not found", exception.getMessage());
            verify(institutionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not update soft-deleted institutions")
        void updateInstitution_softDeleted_throwsException() {
            // Arrange
            testInstitution.setDeletedAt(LocalDateTime.now());
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.updateInstitution(1L, updateDTO)
            );
        }
    }

    @Nested
    @DisplayName("deleteInstitution - Happy Path Tests")
    class DeleteInstitutionHappyPath {

        @Test
        @DisplayName("Should successfully soft delete institution")
        void deleteInstitution_success() {
            // Arrange
            assertNull(testInstitution.getDeletedAt());
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.deleteInstitution(1L);

            // Assert
            assertNotNull(testInstitution.getDeletedAt());
            assertFalse(testInstitution.getUser().getIsActive());
            verify(institutionRepository).save(testInstitution);
        }

        @Test
        @DisplayName("Should deactivate associated user on delete")
        void deleteInstitution_deactivatesUser() {
            // Arrange
            assertTrue(testInstitution.getUser().getIsActive());
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.deleteInstitution(1L);

            // Assert
            assertFalse(testInstitution.getUser().getIsActive());
        }

        @Test
        @DisplayName("Should set deletedAt to current timestamp")
        void deleteInstitution_setsDeletedAtTimestamp() {
            // Arrange
            LocalDateTime before = LocalDateTime.now();
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));

            // Act
            institutionService.deleteInstitution(1L);

            // Assert
            LocalDateTime after = LocalDateTime.now();
            assertNotNull(testInstitution.getDeletedAt());
            assertTrue(testInstitution.getDeletedAt().isAfter(before.minusSeconds(1)));
            assertTrue(testInstitution.getDeletedAt().isBefore(after.plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("deleteInstitution - Unhappy Path Tests")
    class DeleteInstitutionUnhappyPath {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when institution not found")
        void deleteInstitution_notFound_throwsException() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.deleteInstitution(999L)
            );

            assertEquals("Institution not found", exception.getMessage());
            verify(institutionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not delete already soft-deleted institutions")
        void deleteInstitution_alreadyDeleted_throwsException() {
            // Arrange
            testInstitution.setDeletedAt(LocalDateTime.now());
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> institutionService.deleteInstitution(1L)
            );
        }
    }
}
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

@ExtendWith(MockitoExtension.class)
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

    private Institution institution;
    private User user;
    private Role role;
    private CreateInstitutionRequestDTO createDTO;
    private UpdateInstitutionRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(1L)
                .name("INSTITUTION")
                .build();

        user = User.builder()
                .id(1L)
                .email("institution@example.com")
                .name("Test Institution")
                .password("encodedPassword")
                .role(role)
                .isActive(true)
                .build();

        institution = Institution.builder()
                .id(1L)
                .name("Test Institution")
                .address("Test Address")
                .aisheCode("TEST001")
                .user(user)
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

    @Test
    void testCreateInstitution_Success() {
        // Given
        when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(createDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(institutionRepository.save(any(Institution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        institutionService.createInstitution(createDTO);

        // Then
        verify(userRepository).findByEmail(createDTO.email());
        verify(roleRepository).findByName("INSTITUTION");
        verify(passwordEncoder).encode(createDTO.password());
        verify(userRepository).save(any(User.class));
        verify(institutionRepository).save(any(Institution.class));
    }

    @Test
    void testCreateInstitution_EmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.of(user));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> institutionService.createInstitution(createDTO));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).findByEmail(createDTO.email());
        verify(roleRepository, never()).findByName(anyString());
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testCreateInstitution_RoleNotFound() {
        // Given
        when(userRepository.findByEmail(createDTO.email())).thenReturn(Optional.empty());
        when(roleRepository.findByName("INSTITUTION")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> institutionService.createInstitution(createDTO));

        assertEquals("Role not found", exception.getMessage());
        verify(userRepository).findByEmail(createDTO.email());
        verify(roleRepository).findByName("INSTITUTION");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetAllInstitutions_Success() {
        // Given
        List<Institution> institutions = List.of(institution);
        when(institutionRepository.findByDeletedAtIsNull()).thenReturn(institutions);

        // When
        List<InstitutionResponseDTO> result = institutionService.getAllInstitutions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        InstitutionResponseDTO dto = result.get(0);
        assertEquals(institution.getId(), dto.id());
        assertEquals(institution.getName(), dto.name());
        assertEquals(institution.getAddress(), dto.address());
        assertEquals(institution.getAisheCode(), dto.aisheCode());
        assertEquals(institution.getUser().getId(), dto.userId());
        assertEquals(institution.getUser().getIsActive(), dto.isActive());
        assertEquals(institution.getCreatedAt(), dto.createdAt());

        verify(institutionRepository).findByDeletedAtIsNull();
    }

    @Test
    void testGetAllInstitutions_EmptyList() {
        // Given
        when(institutionRepository.findByDeletedAtIsNull()).thenReturn(List.of());

        // When
        List<InstitutionResponseDTO> result = institutionService.getAllInstitutions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(institutionRepository).findByDeletedAtIsNull();
    }

    @Test
    void testGetInstitutionById_Success() {
        // Given
        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        // When
        InstitutionResponseDTO result = institutionService.getInstitutionById(1L);

        // Then
        assertNotNull(result);
        assertEquals(institution.getId(), result.id());
        assertEquals(institution.getName(), result.name());
        assertEquals(institution.getAddress(), result.address());
        assertEquals(institution.getAisheCode(), result.aisheCode());
        assertEquals(institution.getUser().getId(), result.userId());
        assertEquals(institution.getUser().getIsActive(), result.isActive());
        assertEquals(institution.getCreatedAt(), result.createdAt());

        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testGetInstitutionById_NotFound() {
        // Given
        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> institutionService.getInstitutionById(1L));

        assertEquals("Institution not found", exception.getMessage());
        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void testUpdateInstitution_Success() {
        // Given
        UpdateInstitutionRequestDTO dto = new UpdateInstitutionRequestDTO(
                "Updated Name", "Updated Address", "TEST002", true
        );

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institution);

        // When
        institutionService.updateInstitution(1L, dto);

        // Then
        assertEquals("Updated Name", institution.getName());
        assertEquals("Updated Address", institution.getAddress());
        assertEquals("TEST002", institution.getAisheCode());
        assertEquals(true, institution.getUser().getIsActive());

        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
        verify(institutionRepository).save(institution);
    }

    @Test
    void testUpdateInstitution_PartialUpdate() {
        // Given
        UpdateInstitutionRequestDTO dto = new UpdateInstitutionRequestDTO(
                null, "Updated Address", null, null
        );

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institution);

        // When
        institutionService.updateInstitution(1L, dto);

        // Then
        assertEquals("Test Institution", institution.getName()); // unchanged
        assertEquals("Updated Address", institution.getAddress()); // updated
        assertEquals("TEST001", institution.getAisheCode()); // unchanged

        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
        verify(institutionRepository).save(institution);
    }

    @Test
    void testUpdateInstitution_NotFound() {
        // Given
        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> institutionService.updateInstitution(1L, updateDTO));

        assertEquals("Institution not found", exception.getMessage());
        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
        verify(institutionRepository, never()).save(any());
    }

    @Test
    void testDeleteInstitution_Success() {
        // Given
        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institution);

        // When
        institutionService.deleteInstitution(1L);

        // Then
        assertNotNull(institution.getDeletedAt());
        assertFalse(institution.getUser().getIsActive());
        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
        verify(institutionRepository).save(institution);
    }

    @Test
    void testDeleteInstitution_NotFound() {
        // Given
        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> institutionService.deleteInstitution(1L));

        assertEquals("Institution not found", exception.getMessage());
        verify(institutionRepository).findByIdAndDeletedAtIsNull(1L);
        verify(institutionRepository, never()).save(any());
    }
}
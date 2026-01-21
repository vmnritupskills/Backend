package com.example.lms.service;

import com.example.lms.dto.CreateStudentRequestDTO;
import com.example.lms.entity.Institution;
import com.example.lms.entity.Role;
import com.example.lms.entity.Student;
import com.example.lms.entity.User;
import com.example.lms.repository.InstitutionRepository;
import com.example.lms.repository.RoleRepository;
import com.example.lms.repository.StudentRepository;
import com.example.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class StudentServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private Institution institution;
    private Role studentRole;
    private User user;
    private Student student;
    private CreateStudentRequestDTO createDTO;

    @BeforeEach
    void setUp() {
        institution = Institution.builder()
                .id(1L)
                .name("Test Institution")
                .address("Test Address")
                .aisheCode("TEST001")
                .build();

        studentRole = Role.builder()
                .id(1L)
                .name("STUDENT")
                .build();

        user = User.builder()
                .id(1L)
                .email("student@example.com")
                .name("Test Student")
                .password("encodedPassword")
                .role(studentRole)
                .isActive(true)
                .build();

        student = Student.builder()
                .id(1L)
                .regNo("REG001")
                .name("Test Student")
                .email("student@example.com")
                .graduationYear(2025)
                .department("Computer Science")
                .user(user)
                .institution(institution)
                .build();

        createDTO = new CreateStudentRequestDTO(
                1L,
                "REG001",
                "Test Student",
                "student@example.com",
                "password123",
                2025,
                "Computer Science"
        );
    }

    @Test
    void testCreateStudent_Success() {
        // Given
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        studentService.createStudent(createDTO);

        // Then
        verify(institutionRepository).findById(1L);
        verify(studentRepository).findByRegNoAndInstitution_Id("REG001", 1L);
        verify(roleRepository).findByName("STUDENT");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void testCreateStudent_InstitutionNotFound() {
        // Given
        when(institutionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentService.createStudent(createDTO));

        assertEquals("Institution not found", exception.getMessage());
        verify(institutionRepository).findById(1L);
        verify(studentRepository, never()).findByRegNoAndInstitution_Id(anyString(), anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateStudent_StudentAlreadyExists() {
        // Given
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                .thenReturn(Optional.of(student));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> studentService.createStudent(createDTO));

        assertEquals("Student already exists with this RegNo", exception.getMessage());
        verify(institutionRepository).findById(1L);
        verify(studentRepository).findByRegNoAndInstitution_Id("REG001", 1L);
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateStudent_RoleNotFound() {
        // Given
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("STUDENT")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> studentService.createStudent(createDTO));

        assertEquals("STUDENT role not found", exception.getMessage());
        verify(institutionRepository).findById(1L);
        verify(studentRepository).findByRegNoAndInstitution_Id("REG001", 1L);
        verify(roleRepository).findByName("STUDENT");
        verify(userRepository, never()).save(any());
    }
}
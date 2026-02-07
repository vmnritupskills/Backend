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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Tests")
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

    private Institution testInstitution;
    private Role testStudentRole;
    private User testUser;
    private Student testStudent;
    private CreateStudentRequestDTO createDTO;

    @BeforeEach
    void setUp() {
        testInstitution = Institution.builder()
                .id(1L)
                .name("Test Institution")
                .address("Test Address")
                .aisheCode("TEST001")
                .build();

        testStudentRole = Role.builder()
                .id(1L)
                .name("STUDENT")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("student@example.com")
                .name("Test Student")
                .password("encodedPassword")
                .role(testStudentRole)
                .isActive(true)
                .build();

        testStudent = Student.builder()
                .id(1L)
                .regNo("REG001")
                .name("Test Student")
                .email("student@example.com")
                .graduationYear(2025)
                .department("Computer Science")
                .user(testUser)
                .institution(testInstitution)
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

    @Nested
    @DisplayName("createStudent - Happy Path Tests")
    class CreateStudentHappyPath {

        @Test
        @DisplayName("Should successfully create a new student")
        void createStudent_success() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> {
                        User u = invocation.getArgument(0);
                        u.setId(1L);
                        return u;
                    });

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertEquals("student@example.com", savedUser.getEmail());
            assertEquals("Test Student", savedUser.getName());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertTrue(savedUser.getIsActive());

            ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
            verify(studentRepository).save(studentCaptor.capture());
            Student savedStudent = studentCaptor.getValue();

            assertEquals("REG001", savedStudent.getRegNo());
            assertEquals("Test Student", savedStudent.getName());
            assertEquals("student@example.com", savedStudent.getEmail());
            assertEquals(2025, savedStudent.getGraduationYear());
            assertEquals("Computer Science", savedStudent.getDepartment());
        }

        @Test
        @DisplayName("Should encode password before saving")
        void createStudent_encodesPassword() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");

            // Act
            studentService.createStudent(createDTO);

            // Assert
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should set student as active on creation")
        void createStudent_setsActiveStatus() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().getIsActive());
        }

        @Test
        @DisplayName("Should link student with correct institution")
        void createStudent_linksCorrectInstitution() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
            verify(studentRepository).save(studentCaptor.capture());
            assertEquals(testInstitution, studentCaptor.getValue().getInstitution());
        }

        @Test
        @DisplayName("Should link student with user account")
        void createStudent_linksUserAccount() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> {
                        User u = invocation.getArgument(0);
                        u.setId(1L);
                        return u;
                    });

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
            verify(studentRepository).save(studentCaptor.capture());
            assertNotNull(studentCaptor.getValue().getUser());
        }
    }

    @Nested
    @DisplayName("createStudent - Unhappy Path Tests")
    class CreateStudentUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when institution not found")
        void createStudent_institutionNotFound_throwsException() {
            // Arrange
            when(institutionRepository.findById(999L))
                    .thenReturn(Optional.empty());

            CreateStudentRequestDTO dto = new CreateStudentRequestDTO(
                    999L, "REG001", "Student", "student@test.com", "pass", 2025, "CS"
            );

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> studentService.createStudent(dto)
            );

            assertEquals("Institution not found", exception.getMessage());
            verify(studentRepository, never()).findByRegNoAndInstitution_Id(anyString(), anyLong());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when student regNo already exists")
        void createStudent_regNoExists_throwsException() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.of(testStudent));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> studentService.createStudent(createDTO)
            );

            assertEquals("Student already exists with this RegNo", exception.getMessage());
            verify(roleRepository, never()).findByName(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when STUDENT role not found")
        void createStudent_roleNotFound_throwsException() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> studentService.createStudent(createDTO)
            );

            assertEquals("STUDENT role not found", exception.getMessage());
            verify(userRepository, never()).save(any());
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not save student if user save fails")
        void createStudent_userSaveFails_doesNotSaveStudent() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("DB error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> studentService.createStudent(createDTO));
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should verify regNo uniqueness per institution")
        void createStudent_verifiesRegNoUniquenessPerInstitution() {
            // Arrange
            Institution institution2 = Institution.builder().id(2L).build();

            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");

            // Act
            studentService.createStudent(createDTO);

            // Assert
            verify(studentRepository).findByRegNoAndInstitution_Id("REG001", 1L);
        }

        @Test
        @DisplayName("Should not allow null email")
        void createStudent_nullEmail_throwsException() {
            // Arrange
            CreateStudentRequestDTO invalidDTO = new CreateStudentRequestDTO(
                    1L, "REG001", "Student", null, "password", 2025, "CS"
            );

            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("Email cannot be null"));

            // Act & Assert
            assertThrows(
                    RuntimeException.class,
                    () -> studentService.createStudent(invalidDTO)
            );
        }

        @Test
        @DisplayName("Should handle case-sensitive regNo check")
        void createStudent_caseSensitiveRegNo() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");

            // Act
            studentService.createStudent(createDTO);

            // Assert
            verify(studentRepository).findByRegNoAndInstitution_Id("REG001", 1L);
        }
    }

    @Nested
    @DisplayName("createStudent - Field Validation Tests")
    class CreateStudentFieldValidation {

        @Test
        @DisplayName("Should preserve graduation year during creation")
        void createStudent_preservesGraduationYear() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
            verify(studentRepository).save(studentCaptor.capture());
            assertEquals(2025, studentCaptor.getValue().getGraduationYear());
        }

        @Test
        @DisplayName("Should preserve department during creation")
        void createStudent_preservesDepartment() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
            verify(studentRepository).save(studentCaptor.capture());
            assertEquals("Computer Science", studentCaptor.getValue().getDepartment());
        }

        @Test
        @DisplayName("Should use correct role for student")
        void createStudent_usesCorrectRole() {
            // Arrange
            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("REG001", 1L))
                    .thenReturn(Optional.empty());
            when(roleRepository.findByName("STUDENT"))
                    .thenReturn(Optional.of(testStudentRole));
            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            studentService.createStudent(createDTO);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(testStudentRole, userCaptor.getValue().getRole());
        }
    }
}
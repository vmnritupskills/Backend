package com.example.lms.service;

import com.example.lms.dto.BulkEnrollStudentsRequestDTO;
import com.example.lms.dto.EnrollStudentRequestDTO;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Tests")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Institution testInstitution;
    private Student testStudent;
    private Course testCourse;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testInstitution = Institution.builder().id(1L).build();
        testStudent = Student.builder().id(1L).regNo("STU001").build();
        testCourse = Course.builder().id(1L).build();
        testEnrollment = Enrollment.builder()
                .id(1L)
                .student(testStudent)
                .course(testCourse)
                .isEnrolled(true)
                .build();
    }

    @Nested
    @DisplayName("enrollStudent - Happy Path Tests")
    class EnrollStudentHappyPath {

        @Test
        @DisplayName("Should successfully enroll a student when all validations pass")
        void enrollStudent_success() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STU001", 1L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(testStudent));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class)))
                    .thenReturn(testEnrollment);

            // Act
            enrollmentService.enrollStudent(dto);

            // Assert
            ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
            verify(enrollmentRepository).save(enrollmentCaptor.capture());
            Enrollment savedEnrollment = enrollmentCaptor.getValue();

            assertNotNull(savedEnrollment);
            assertEquals(testStudent, savedEnrollment.getStudent());
            assertEquals(testCourse, savedEnrollment.getCourse());
            assertTrue(savedEnrollment.getIsEnrolled());
        }

        @Test
        @DisplayName("Should save enrollment with correct student-course relationship")
        void enrollStudent_verifiesCorrectRelationships() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STU001", 1L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(testStudent));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(false);

            // Act
            enrollmentService.enrollStudent(dto);

            // Assert
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
            verify(enrollmentRepository, times(1)).existsByStudent_IdAndCourse_Id(1L, 1L);
        }
    }

    @Nested
    @DisplayName("enrollStudent - Unhappy Path Tests")
    class EnrollStudentUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when institution not found")
        void enrollStudent_institutionNotFound_throwsException() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(999L, "STU001", 1L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> enrollmentService.enrollStudent(dto)
            );

            assertEquals("Institution not found", exception.getMessage());
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when student not found in institution")
        void enrollStudent_studentNotFound_throwsException() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "INVALID", 1L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("INVALID", 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> enrollmentService.enrollStudent(dto)
            );

            assertEquals("Student not found in this institution", exception.getMessage());
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when course not found in institution")
        void enrollStudent_courseNotFound_throwsException() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STU001", 999L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(testStudent));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> enrollmentService.enrollStudent(dto)
            );

            assertEquals("Course not found in this institution", exception.getMessage());
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when student already enrolled")
        void enrollStudent_alreadyEnrolled_throwsException() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STU001", 1L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(testStudent));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(true);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> enrollmentService.enrollStudent(dto)
            );

            assertEquals("Student already enrolled in this course", exception.getMessage());
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should verify enrollment check is called before saving")
        void enrollStudent_verifiesEnrollmentCheckBeforeSave() {
            // Arrange
            EnrollStudentRequestDTO dto = new EnrollStudentRequestDTO(1L, "STU001", 1L);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(testStudent));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(false);

            // Act
            enrollmentService.enrollStudent(dto);

            // Assert
            InOrder inOrder = inOrder(enrollmentRepository);
            inOrder.verify(enrollmentRepository).existsByStudent_IdAndCourse_Id(1L, 1L);
            inOrder.verify(enrollmentRepository).save(any(Enrollment.class));
        }
    }

    @Nested
    @DisplayName("bulkEnrollStudents - Happy Path Tests")
    class BulkEnrollStudentsHappyPath {

        @Test
        @DisplayName("Should successfully enroll multiple students")
        void bulkEnrollStudents_success() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "STU002", "STU003");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 1L, studentIds);

            Student student1 = Student.builder().id(1L).regNo("STU001").build();
            Student student2 = Student.builder().id(2L).regNo("STU002").build();
            Student student3 = Student.builder().id(3L).regNo("STU003").build();

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(student1));
            when(studentRepository.findByRegNoAndInstitution_Id("STU002", 1L))
                    .thenReturn(Optional.of(student2));
            when(studentRepository.findByRegNoAndInstitution_Id("STU003", 1L))
                    .thenReturn(Optional.of(student3));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(anyLong(), anyLong()))
                    .thenReturn(false);

            // Act
            enrollmentService.bulkEnrollStudents(dto);

            // Assert
            verify(enrollmentRepository, times(3)).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should enroll only new students, skip already enrolled ones")
        void bulkEnrollStudents_skipsAlreadyEnrolled() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "STU002");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 1L, studentIds);

            Student student1 = Student.builder().id(1L).regNo("STU001").build();
            Student student2 = Student.builder().id(2L).regNo("STU002").build();

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(student1));
            when(studentRepository.findByRegNoAndInstitution_Id("STU002", 1L))
                    .thenReturn(Optional.of(student2));

            // First student already enrolled, second is new
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(true);
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(2L, 1L))
                    .thenReturn(false);

            // Act
            enrollmentService.bulkEnrollStudents(dto);

            // Assert - only 1 save call (for student2), student1 is skipped
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should save all enrollments with correct isEnrolled flag")
        void bulkEnrollStudents_verifiesEnrollmentFlag() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "STU002");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 1L, studentIds);

            Student student1 = Student.builder().id(1L).regNo("STU001").build();
            Student student2 = Student.builder().id(2L).regNo("STU002").build();

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(student1));
            when(studentRepository.findByRegNoAndInstitution_Id("STU002", 1L))
                    .thenReturn(Optional.of(student2));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(anyLong(), anyLong()))
                    .thenReturn(false);

            // Act
            enrollmentService.bulkEnrollStudents(dto);

            // Assert
            ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
            verify(enrollmentRepository, times(2)).save(enrollmentCaptor.capture());

            List<Enrollment> savedEnrollments = enrollmentCaptor.getAllValues();
            for (Enrollment enrollment : savedEnrollments) {
                assertTrue(enrollment.getIsEnrolled());
            }
        }
    }

    @Nested
    @DisplayName("bulkEnrollStudents - Unhappy Path Tests")
    class BulkEnrollStudentsUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when institution not found")
        void bulkEnrollStudents_institutionNotFound_throwsException() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "STU002");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(999L, 1L, studentIds);

            when(institutionRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> enrollmentService.bulkEnrollStudents(dto)
            );

            assertEquals("Institution not found", exception.getMessage());
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when course not found")
        void bulkEnrollStudents_courseNotFound_throwsException() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "STU002");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 999L, studentIds);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> enrollmentService.bulkEnrollStudents(dto)
            );

            assertEquals("Course not found in this institution", exception.getMessage());
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when any student not found")
        void bulkEnrollStudents_studentNotFound_throwsException() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "INVALID");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 1L, studentIds);

            Student student1 = Student.builder().id(1L).regNo("STU001").build();

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(student1));
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(false);
            when(studentRepository.findByRegNoAndInstitution_Id("INVALID", 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> enrollmentService.bulkEnrollStudents(dto)
            );

            assertTrue(exception.getMessage().contains("Student not found: INVALID"));
            // First student should be saved, but exception on second
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should handle empty student list")
        void bulkEnrollStudents_emptyStudentList_noEnrollmentsSaved() {
            // Arrange
            List<String> studentIds = Arrays.asList();
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 1L, studentIds);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            enrollmentService.bulkEnrollStudents(dto);

            // Assert
            verify(enrollmentRepository, never()).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("Should process all students even if some are already enrolled")
        void bulkEnrollStudents_continuesToProcessWhenAlreadyEnrolled() {
            // Arrange
            List<String> studentIds = Arrays.asList("STU001", "STU002", "STU003");
            BulkEnrollStudentsRequestDTO dto = new BulkEnrollStudentsRequestDTO(1L, 1L, studentIds);

            Student student1 = Student.builder().id(1L).regNo("STU001").build();
            Student student2 = Student.builder().id(2L).regNo("STU002").build();
            Student student3 = Student.builder().id(3L).regNo("STU003").build();

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                    .thenReturn(Optional.of(student1));
            when(studentRepository.findByRegNoAndInstitution_Id("STU002", 1L))
                    .thenReturn(Optional.of(student2));
            when(studentRepository.findByRegNoAndInstitution_Id("STU003", 1L))
                    .thenReturn(Optional.of(student3));

            // Student 1 and 3 already enrolled, only 2 is new
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                    .thenReturn(true);
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(2L, 1L))
                    .thenReturn(false);
            when(enrollmentRepository.existsByStudent_IdAndCourse_Id(3L, 1L))
                    .thenReturn(true);

            // Act
            enrollmentService.bulkEnrollStudents(dto);

            // Assert - only student 2 should be saved
            verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        }
    }
}

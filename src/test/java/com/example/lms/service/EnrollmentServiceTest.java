package com.example.lms.service;

import com.example.lms.dto.BulkEnrollStudentsRequestDTO;
import com.example.lms.dto.EnrollStudentRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.Institution;
import com.example.lms.entity.Student;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.InstitutionRepository;
import com.example.lms.repository.StudentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    /* ================= SINGLE ENROLL ================= */

    @Test
    void enrollStudent_success() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 10L);

        Institution institution = Institution.builder().id(1L).build();
        Student student = Student.builder().regNo("STU001").build();
        Course course = Course.builder().id(10L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourse_Id("STU001", 10L))
                .thenReturn(false);

        enrollmentService.enrollStudent(dto);

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_fail_institution_not_found() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 10L);

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentService.enrollStudent(dto)
        );
    }

    @Test
    void enrollStudent_fail_student_not_found() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 10L);

        Institution institution = Institution.builder().id(1L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentService.enrollStudent(dto)
        );
    }

    @Test
    void enrollStudent_fail_course_not_found() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 10L);

        Institution institution = Institution.builder().id(1L).build();
        Student student = Student.builder().regNo("STU001").build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentService.enrollStudent(dto)
        );
    }

    @Test
    void enrollStudent_fail_already_enrolled() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 10L);

        Institution institution = Institution.builder().id(1L).build();
        Student student = Student.builder().regNo("STU001").build();
        Course course = Course.builder().id(10L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudentIdAndCourse_Id("STU001", 10L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> enrollmentService.enrollStudent(dto)
        );

        verify(enrollmentRepository, never()).save(any());
    }

    /* ================= BULK ENROLL ================= */

    @Test
    void bulkEnrollStudents_success() {

        BulkEnrollStudentsRequestDTO dto =
                new BulkEnrollStudentsRequestDTO(
                        1L,
                        10L,
                        List.of("STU001", "STU002")
                );

        Institution institution = Institution.builder().id(1L).build();
        Course course = Course.builder().id(10L).build();

        Student s1 = Student.builder().regNo("STU001").build();
        Student s2 = Student.builder().regNo("STU002").build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        when(studentRepository.findByRegNoAndInstitution_Id(anyString(), eq(1L)))
                .thenReturn(Optional.of(s1), Optional.of(s2));

        when(enrollmentRepository.existsByStudentIdAndCourse_Id(anyString(), eq(10L)))
                .thenReturn(false);

        enrollmentService.bulkEnrollStudents(dto);

        verify(enrollmentRepository, times(2)).save(any(Enrollment.class));
    }

    @Test
    void bulkEnrollStudents_skip_already_enrolled() {

        BulkEnrollStudentsRequestDTO dto =
                new BulkEnrollStudentsRequestDTO(
                        1L,
                        10L,
                        List.of("STU001", "STU002")
                );

        Institution institution = Institution.builder().id(1L).build();
        Course course = Course.builder().id(10L).build();

        Student s1 = Student.builder().regNo("STU001").build();
        Student s2 = Student.builder().regNo("STU002").build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        when(studentRepository.findByRegNoAndInstitution_Id(anyString(), eq(1L)))
                .thenReturn(Optional.of(s1), Optional.of(s2));

        when(enrollmentRepository.existsByStudentIdAndCourse_Id("STU001", 10L))
                .thenReturn(true);

        when(enrollmentRepository.existsByStudentIdAndCourse_Id("STU002", 10L))
                .thenReturn(false);

        enrollmentService.bulkEnrollStudents(dto);

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void bulkEnrollStudents_fail_institution_not_found() {

        BulkEnrollStudentsRequestDTO dto =
                new BulkEnrollStudentsRequestDTO(
                        1L,
                        10L,
                        List.of("STU001")
                );

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentService.bulkEnrollStudents(dto)
        );
    }

    @Test
    void bulkEnrollStudents_fail_course_not_found() {

        BulkEnrollStudentsRequestDTO dto =
                new BulkEnrollStudentsRequestDTO(
                        1L,
                        10L,
                        List.of("STU001")
                );

        Institution institution = Institution.builder().id(1L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentService.bulkEnrollStudents(dto)
        );
    }

    @Test
    void bulkEnrollStudents_fail_student_not_found() {

        BulkEnrollStudentsRequestDTO dto =
                new BulkEnrollStudentsRequestDTO(
                        1L,
                        10L,
                        List.of("STU001")
                );

        Institution institution = Institution.builder().id(1L).build();
        Course course = Course.builder().id(10L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentService.bulkEnrollStudents(dto)
        );
    }
}

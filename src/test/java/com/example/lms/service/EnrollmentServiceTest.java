package com.example.lms.service;

import com.example.lms.dto.EnrollStudentRequestDTO;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void enrollStudent_success() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 1L);

        Institution institution = Institution.builder().id(1L).build();
        Student student = Student.builder().id(1L).regNo("STU001").build();
        Course course = Course.builder().id(1L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                .thenReturn(false);

        enrollmentService.enrollStudent(dto);

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_alreadyEnrolled_throwsException() {

        EnrollStudentRequestDTO dto =
                new EnrollStudentRequestDTO(1L, "STU001", 1L);

        Institution institution = Institution.builder().id(1L).build();
        Student student = Student.builder().id(1L).regNo("STU001").build();
        Course course = Course.builder().id(1L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(studentRepository.findByRegNoAndInstitution_Id("STU001", 1L))
                .thenReturn(Optional.of(student));

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                .thenReturn(Optional.of(course));

        when(enrollmentRepository.existsByStudent_IdAndCourse_Id(1L, 1L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> enrollmentService.enrollStudent(dto)
        );
    }
}

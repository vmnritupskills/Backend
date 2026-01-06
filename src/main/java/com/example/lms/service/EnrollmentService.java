package com.example.lms.service;

import com.example.lms.dto.BulkEnrollStudentsRequestDTO;
import com.example.lms.dto.EnrollStudentRequestDTO;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final CourseRepository courseRepository;

    /* ================= SINGLE ENROLL ================= */
    @Transactional
    public void enrollStudent(EnrollStudentRequestDTO dto) {

        Institution institution = institutionRepository
                .findByIdAndDeletedAtIsNull(dto.institutionId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Institution not found"));

        Student student = studentRepository
                .findByRegNoAndInstitution_Id(dto.studentId(), institution.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found in this institution"));

        Course course = courseRepository
                .findByIdAndInstitution_IdAndDeletedAtIsNull(
                        dto.courseId(),
                        institution.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found in this institution"));

        if (enrollmentRepository.existsByStudentIdAndCourse_Id(
                student.getRegNo(),
                course.getId())) {
            throw new IllegalStateException("Student already enrolled in this course");
        }

        enrollmentRepository.save(
                Enrollment.builder()
                        .studentId(student.getRegNo())
                        .course(course)
                        .isEnrolled(true)
                        .build()
        );
    }

    /* ================= BULK ENROLL ================= */
    @Transactional
    public void bulkEnrollStudents(BulkEnrollStudentsRequestDTO dto) {

        Institution institution = institutionRepository
                .findByIdAndDeletedAtIsNull(dto.institutionId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Institution not found"));

        Course course = courseRepository
                .findByIdAndInstitution_IdAndDeletedAtIsNull(
                        dto.courseId(),
                        institution.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found in this institution"));

        for (String regNo : dto.studentIds()) {

            Student student = studentRepository
                    .findByRegNoAndInstitution_Id(regNo, institution.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Student not found: " + regNo));

            if (enrollmentRepository.existsByStudentIdAndCourse_Id(
                    student.getRegNo(),
                    course.getId())) {
                continue;
            }

            enrollmentRepository.save(
                    Enrollment.builder()
                            .studentId(student.getRegNo())
                            .course(course)
                            .isEnrolled(true)
                            .build()
            );
        }
    }
}

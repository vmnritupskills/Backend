package com.example.lms.service;

import com.example.lms.dto.CourseDTO;
import com.example.lms.dto.StudentCourseResponseDTO;
import com.example.lms.entity.Course;
import com.example.lms.entity.Student;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public StudentCourseResponseDTO getMyCourses(Long userId) {

        // userId → student
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        // student → enrollment → course
        List<CourseDTO> courses =
                enrollmentRepository
                        .findActiveEnrollmentsWithCourse(student.getId())
                        .stream()
                        .map(enrollment -> {
                            Course c = enrollment.getCourse();
                            return new CourseDTO(
                                    c.getId(),
                                    c.getName(),
                                    c.getCourseCode(),
                                    c.getDuration(),
                                    c.getSemester()
                            );
                        })
                        .toList();

        // 3️⃣ response
        return new StudentCourseResponseDTO(
                student.getId(),
                courses
        );
    }
}

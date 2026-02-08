package com.example.lms.service;

import com.example.lms.dto.CourseCompletionDTO;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.Student;
import com.example.lms.repository.CourseSubtopicRepository;
import com.example.lms.repository.CourseTopicRepository;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentCourseCompletionService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseTopicRepository topicRepository;
    private final CourseSubtopicRepository subtopicRepository;

    @Transactional(readOnly = true)
    public CourseCompletionDTO getCourseCompletion(
            Long userId,
            Long courseId
    ) {

        // 1️⃣ userId → student
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not found"));

        // 2️⃣ validate enrollment
        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_IdAndIsEnrolledTrue(
                        student.getId(),
                        courseId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Student not enrolled in this course"));

        Course course = enrollment.getCourse();

        // 3️⃣ course structure
        long totalTopics =
                topicRepository.countByCourse_Id(courseId);

        long totalSubtopics =
                subtopicRepository.countByTopic_Course_Id(courseId);

        // 🚨 No student progress table yet
        int completionPercentage = 0;

        return new CourseCompletionDTO(
                student.getId(),
                course.getId(),
                course.getName(),
                totalTopics,
                totalSubtopics,
                completionPercentage
        );
    }
}

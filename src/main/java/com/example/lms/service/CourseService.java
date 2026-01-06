package com.example.lms.service;

import com.example.lms.dto.CreateCourseRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.entity.Institution;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.InstitutionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final InstitutionRepository institutionRepository;
    private final CourseRepository courseRepository;
    private final S3Service s3Service;

    /* ================= CREATE ================= */
    @Transactional
    public void createCourse(CreateCourseRequestDTO dto, Long institutionId) {

        Institution institution = institutionRepository
                .findByIdAndDeletedAtIsNull(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        if (courseRepository.existsByCourseCodeAndInstitution_Id(
                dto.courseCode(), institutionId)) {
            throw new IllegalStateException("Course code already exists");
        }

        String syllabusUrl = null;
        if (dto.syllabus() != null && !dto.syllabus().isEmpty()) {
            syllabusUrl = s3Service.uploadSyllabus(dto.syllabus());
        }

        Course course = Course.builder()
                .name(dto.name())
                .courseCode(dto.courseCode())
                .duration(dto.duration())
                .semester(dto.semester())
                .syllabusUrl(syllabusUrl)
                .institution(institution)
                .build();

        courseRepository.save(course);
    }

    /* ================= GET ALL ================= */
    public List<Course> getAllCourses(Long institutionId) {

        return courseRepository
                .findByInstitution_IdAndDeletedAtIsNull(institutionId);
    }

    /* ================= GET BY ID ================= */
    public Course getCourseById(Long courseId, Long institutionId) {

        return courseRepository
                .findByIdAndInstitution_IdAndDeletedAtIsNull(courseId, institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    /* ================= UPDATE ================= */
    @Transactional
    public void updateCourse(
            Long courseId,
            CreateCourseRequestDTO dto,
            Long institutionId) {

        Course course = getCourseById(courseId, institutionId);

        if (dto.name() != null) course.setName(dto.name());
        if (dto.duration() != null) course.setDuration(dto.duration());
        if (dto.semester() != null) course.setSemester(dto.semester());

        if (dto.syllabus() != null && !dto.syllabus().isEmpty()) {
            course.setSyllabusUrl(s3Service.uploadSyllabus(dto.syllabus()));
        }

        courseRepository.save(course);
    }

    /* ================= DELETE (SOFT) ================= */
    @Transactional
    public void deleteCourse(Long courseId, Long institutionId) {

        Course course = getCourseById(courseId, institutionId);
        course.setDeletedAt(LocalDateTime.now());

        courseRepository.save(course);
    }
}

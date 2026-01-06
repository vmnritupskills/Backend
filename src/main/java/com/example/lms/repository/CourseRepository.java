package com.example.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {


    Optional<Course> findByCourseCode(String courseCode);

        boolean existsByCourseCodeAndInstitution_Id(String courseCode, Long institutionId);

        List<Course> findByInstitution_IdAndDeletedAtIsNull(Long institutionId);

        Optional<Course> findByIdAndInstitution_IdAndDeletedAtIsNull(
                Long courseId,
                Long institutionId
        );


}

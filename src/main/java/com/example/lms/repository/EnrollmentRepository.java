package com.example.lms.repository;

import com.example.lms.dto.EnrolledStudentResponseDTO;
import com.example.lms.entity.Enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /* ================= EXISTENCE CHECKS ================= */

    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    boolean existsByStudent_RegNoAndCourse_Id(String regNo, Long courseId);

    /* ================= DASHBOARD QUERIES ================= */

    @Query("""
        SELECT COUNT(DISTINCT e.student.id)
        FROM Enrollment e
        JOIN e.course c
        JOIN InstitutionCourseManager icm
             ON icm.course = c
        WHERE icm.contentManager.id = :contentManagerId
          AND e.isEnrolled = true
    """)
    Long countStudentsUnderContentManager(
            @Param("contentManagerId") Long contentManagerId
    );

    @Query("""
    SELECT DISTINCT new com.example.lms.dto.EnrolledStudentResponseDTO(
        s.id,
        s.name,
        s.email,
        c.name
    )
    FROM Enrollment e
    JOIN e.student s
    JOIN e.course c
    JOIN InstitutionCourseManager icm
         ON icm.course = c
    WHERE icm.contentManager.id = :contentManagerId
      AND e.isEnrolled = true
      AND s.deletedAt IS NULL
""")
    List<EnrolledStudentResponseDTO>
    findStudentsUnderContentManager(
            @Param("contentManagerId") Long contentManagerId
    );

}

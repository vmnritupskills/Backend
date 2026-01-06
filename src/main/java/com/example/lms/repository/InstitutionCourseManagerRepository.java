package com.example.lms.repository;

import com.example.lms.entity.InstitutionCourseManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionCourseManagerRepository
        extends JpaRepository<InstitutionCourseManager, Long> {

    /* ================= EXISTS ================= */

    boolean existsByCourseIdAndContentManagerId(
            Long courseId,
            Long contentManagerId
    );

    /* ================= FIND ================= */

    Optional<InstitutionCourseManager>
    findByCourseIdAndContentManagerId(
            Long courseId,
            Long contentManagerId
    );

    List<InstitutionCourseManager>
    findByContentManagerId(
            Long contentManagerId
    );

    /* ================= DELETE ================= */

    void deleteByContentManagerId(
            Long contentManagerId
    );


}

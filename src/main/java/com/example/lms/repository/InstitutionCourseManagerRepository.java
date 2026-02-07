package com.example.lms.repository;

import com.example.lms.entity.InstitutionCourseManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InstitutionCourseManagerRepository
        extends JpaRepository<InstitutionCourseManager, Long> {

    /* ================= EXISTS ================= */

    boolean existsByCourse_IdAndContentManager_Id(
            Long courseId,
            Long contentManagerId
    );

    /* ================= FIND ================= */

    Optional<InstitutionCourseManager>
    findByCourse_IdAndContentManager_Id(
            Long courseId,
            Long contentManagerId
    );

    List<InstitutionCourseManager>
    findByContentManager_Id(
            Long contentManagerId
    );

    /* ================= DELETE ================= */

    void deleteByContentManager_Id(
            Long contentManagerId
    );

    @Query("""
    select icm.course.id
    from InstitutionCourseManager icm
    where icm.contentManager.id = :cmId
""")
    List<Long> findCourseIdsByContentManagerId(Long cmId);

}

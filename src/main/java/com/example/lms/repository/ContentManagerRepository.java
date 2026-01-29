package com.example.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.lms.entity.ContentManager;

import java.util.List;
import java.util.Optional;

public interface ContentManagerRepository extends JpaRepository<ContentManager, Long> {

    Optional<ContentManager> findByIdAndInstitutionId(Long id, Long institutionId);

    List<ContentManager> findByInstitutionId(Long institutionId);

    // 🔹 TOTAL content managers (all institutions)
    long count();

    // 🔹 ACTIVE content managers (via linked User)
    @Query("""
        SELECT COUNT(cm)
        FROM ContentManager cm
        WHERE cm.user.isActive = true
    """)
    long countActiveContentManagers();
}

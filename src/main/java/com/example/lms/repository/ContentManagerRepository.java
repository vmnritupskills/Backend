package com.example.lms.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.entity.ContentManager;

import java.util.List;
import java.util.Optional;

public interface ContentManagerRepository extends JpaRepository<ContentManager, Long> {


    Optional<ContentManager> findByIdAndInstitutionId(Long id, Long institutionId);
    @EntityGraph(attributePaths = { "user" })
    List<ContentManager> findByInstitutionId(Long institutionId);


}


package com.example.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.lms.entity.Institution;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByUserId(Long userId);


    @EntityGraph(attributePaths = { "user" })
    List<Institution> findByDeletedAtIsNull();

    @EntityGraph(attributePaths = { "user" })
    Optional<Institution> findByIdAndDeletedAtIsNull(Long id);
}

package com.example.lms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lms.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByTokenAndIsActiveTrue(String token);
}

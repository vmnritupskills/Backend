package com.example.login.repository;

import com.example.login.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, Long> {

    // FIXED — must use "active"
    List<Session> findByUserIdAndActive(Long userId, boolean active);

    List<Session> findByUserId(UUID userId);
}

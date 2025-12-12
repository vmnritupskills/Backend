package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String token;

    private String refreshToken;

    @Column(name = "device_info")
    private String deviceInfo;

    private Instant expiresAt;

    @Column(name = "is_active")
    private boolean active;

    private Instant createdAt;
}


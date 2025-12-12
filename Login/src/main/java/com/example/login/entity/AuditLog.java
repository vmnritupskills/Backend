package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private String performedBy;

    @Column(length = 2000)
    private String metadata;

    private Instant timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = Instant.now();
    }
}

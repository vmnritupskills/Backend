package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "content_managers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // <-- changed to LONG

    @Column(name = "institution_id", columnDefinition = "uuid", nullable = false)
    private String institutionId;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String email;   // <-- NEW FIELD

    @Column(name = "created_by", columnDefinition = "uuid")
    private String createdBy;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}

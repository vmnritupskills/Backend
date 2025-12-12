package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ❗ ID changed from UUID → Long

    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "roll_number")
    private String rollNumber;

    private String branch;

    @Column(name = "date_of_admission")
    private String dateOfAdmission;

    @Column(name = "batch_id")
    private Long batchId;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

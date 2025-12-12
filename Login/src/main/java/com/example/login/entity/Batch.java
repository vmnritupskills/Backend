package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_year_id", nullable = false)
    private Long batchYearId;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private String name;

    private Instant startDate;
    private Instant endDate;

    @Column(name = "created_by")
    private Long createdBy;

    private Instant createdAt;
}


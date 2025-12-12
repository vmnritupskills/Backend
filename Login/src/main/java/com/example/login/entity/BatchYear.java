package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "batch_years")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long institutionId;

    private String name;

    private Instant startYear;
    private Instant endYear;

    private Long createdBy;

    private Instant createdAt;
}


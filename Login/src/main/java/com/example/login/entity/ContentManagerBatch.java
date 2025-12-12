package com.example.login.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "content_manager_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentManagerBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO-INCREMENT LONG
    private Long id;

    @Column(name = "content_manager_id", nullable = false)
    private Long contentManagerId;  // FK → ContentManager.id (LONG)

    @Column(name = "batch_id", nullable = false)
    private Long batchId; // FK → Batch.id (LONG)
}

package com.example.login.service;

import com.example.login.dto.content.AssignBatchReq;
import com.example.login.dto.content.CreateContentManagerReq;
import com.example.login.entity.Batch;
import com.example.login.entity.ContentManager;
import com.example.login.entity.ContentManagerBatch;
import com.example.login.entity.Role;
import com.example.login.entity.User;
import com.example.login.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContentManagerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ContentManagerRepository contentManagerRepository;
    private final BatchRepository batchRepository;
    private final ContentManagerBatchRepository contentManagerBatchRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // CREATE CONTENT MANAGER
    // ============================================================
    @Transactional
    public Object createContentManager(CreateContentManagerReq req, Long institutionId, Long adminId) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. Fetch role
        Role cmRole = roleRepository.findByName("CONTENT_MANAGER")
                .orElseThrow(() -> new IllegalStateException("CONTENT_MANAGER role missing"));

        // 3. Create USER entry
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .institutionId(institutionId)     // Long
                .roleId(cmRole.getId())           // Long
                .active(true)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        // 4. Create ContentManager entry
        ContentManager cm = ContentManager.builder()
                .userId(user.getId().toString())              // convert Long → String
                .email(user.getEmail())
                .institutionId(institutionId.toString())      // Long → String
                .createdBy(adminId.toString())                // Long → String
                .createdAt(Instant.now())
                .build();

        contentManagerRepository.save(cm);

        return Map.of(
                "message", "Content Manager created successfully",
                "contentManagerId", cm.getId(),
                "userId", user.getId(),
                "email", user.getEmail()
        );
    }

    // ============================================================
    // ASSIGN MULTIPLE BATCHES
    // ============================================================
    @Transactional
    public Object assignBatches(Long cmId, AssignBatchReq req, Long institutionId, Long adminId) {

        ContentManager cm = contentManagerRepository.findById(cmId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid content manager ID"));

        if (!cm.getInstitutionId().equals(institutionId.toString())) {
            throw new IllegalStateException("This content manager does not belong to your institution");
        }

        if (req.getBatchIds() == null || req.getBatchIds().isEmpty()) {
            throw new IllegalArgumentException("batchIds cannot be empty");
        }

        List<Long> assignedIds = new ArrayList<>();
        List<ContentManagerBatch> savedList = new ArrayList<>();

        for (Long batchId : req.getBatchIds()) {

            Batch batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid batch ID: " + batchId));

            if (!batch.getInstitutionId().equals(institutionId)) {
                throw new IllegalStateException("Batch does not belong to your institution");
            }

            boolean exists = contentManagerBatchRepository
                    .existsByContentManagerIdAndBatchId(cmId, batchId);

            if (exists) continue;

            ContentManagerBatch cmb = ContentManagerBatch.builder()
                    .contentManagerId(cmId)         // LONG field
                    .batchId(batchId)
                    .build();

            contentManagerBatchRepository.save(cmb);

            savedList.add(cmb);
            assignedIds.add(batchId);
        }

        return Map.of(
                "message", "Batches assigned successfully",
                "assignedCount", savedList.size(),
                "assignedBatchIds", assignedIds,
                "contentManagerId", cmId
        );
    }
}

package com.example.login.controller;

import com.example.login.dto.content.AssignBatchReq;
import com.example.login.dto.content.CreateContentManagerReq;
import com.example.login.service.ContentManagerService;
import com.example.login.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/content-managers")
@RequiredArgsConstructor
public class ContentManagerController {

    private final ContentManagerService contentManagerService;
    private final JwtUtil jwtUtil;

    // ===================== CREATE CONTENT MANAGER =====================
    @PostMapping("/create")
    public ResponseEntity<?> createContentManager(
            @RequestHeader("Authorization") String bearer,
            @RequestBody CreateContentManagerReq req
    ) {
        var claims = jwtUtil.parseToken(bearer.substring(7));

        if (!"INSTITUTION_ADMIN".equals(claims.get("role"))) {
            return ResponseEntity.status(403).body("Only INSTITUTION_ADMIN can create content managers");
        }

        // Convert to Long
        Long institutionId = Long.valueOf(claims.get("institutionId").toString());
        Long adminId = Long.valueOf(claims.getSubject());

        return ResponseEntity.ok(
                contentManagerService.createContentManager(req, institutionId, adminId)
        );
    }

    // ===================== ASSIGN BATCHES =====================
    @PostMapping("/{cmId}/assign-batches")
    public ResponseEntity<?> assignBatches(
            @RequestHeader("Authorization") String bearer,
            @PathVariable Long cmId,
            @RequestBody AssignBatchReq req
    ) {
        var claims = jwtUtil.parseToken(bearer.substring(7));

        if (!"INSTITUTION_ADMIN".equals(claims.get("role"))) {
            return ResponseEntity.status(403).body("Only INSTITUTION_ADMIN can assign batches");
        }

        // Convert to Long
        Long institutionId = Long.valueOf(claims.get("institutionId").toString());
        Long adminId = Long.valueOf(claims.getSubject());

        return ResponseEntity.ok(
                contentManagerService.assignBatches(cmId, req, institutionId, adminId)
        );
    }
}

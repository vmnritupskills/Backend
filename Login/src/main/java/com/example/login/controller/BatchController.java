package com.example.login.controller;

import com.example.login.dto.batch.CreateBatchReq;
import com.example.login.service.BatchService;
import com.example.login.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createBatch(
            @RequestHeader("Authorization") String bearer,
            @Valid @RequestBody CreateBatchReq req
    ) {
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Invalid Authorization header");
        }

        String token = bearer.substring(7);
        var claims = jwtUtil.parseToken(token);

        String role = (String) claims.get("role");

        if (!"INSTITUTION_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Only INSTITUTION_ADMIN can create batches");
        }

        Long createdBy = Long.parseLong(claims.getSubject());   // 🔥 FIXED (UUID → Long)

        return ResponseEntity.ok(batchService.createBatch(req, createdBy));
    }
}

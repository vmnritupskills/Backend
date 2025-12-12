package com.example.login.controller;

import com.example.login.dto.batchyear.BatchYearCreateReq;
import com.example.login.service.BatchYearService;
import com.example.login.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batch-years")
@RequiredArgsConstructor
public class BatchYearController {

    private final BatchYearService batchYearService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createBatchYear(
            @RequestHeader("Authorization") String bearer,
            @RequestBody BatchYearCreateReq req
    ) {

        String token = bearer.substring(7);
        var claims = jwtUtil.parseToken(token);

        Long createdBy = Long.parseLong(claims.getSubject());   // 🔥 FIXED

        return ResponseEntity.ok(batchYearService.createBatchYear(req, createdBy));
    }
}

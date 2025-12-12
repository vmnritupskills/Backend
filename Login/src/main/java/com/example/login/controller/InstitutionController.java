package com.example.login.controller;

import com.example.login.dto.institution.CreateInstitutionReq;
import com.example.login.service.InstitutionService;
import com.example.login.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createInstitution(
            @RequestHeader("Authorization") String bearer,
            @RequestBody CreateInstitutionReq req
    ) {
        var claims = jwtUtil.parseToken(bearer.substring(7));

        if (!"ADMIN".equals(claims.get("role"))) {
            return ResponseEntity.status(403).body("Only ADMIN can create institutions");
        }

        Long adminId = Long.valueOf(claims.getSubject());

        return ResponseEntity.ok(
                institutionService.createInstitution(req, adminId)
        );
    }
}

package com.example.login.controller;

import com.example.login.dto.auth.*;
import com.example.login.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterAdminReq req) {
        return ResponseEntity.ok(authService.registerAdmin(req));
    }


    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody RegisterStudentReq req) {
        return ResponseEntity.ok(authService.registerStudent(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutReq req) {
        authService.logout(req.getSessionId());
        return ResponseEntity.ok().body("Logged out");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshReq req) {
        return ResponseEntity.ok(authService.refresh(req.getRefreshToken()));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestHeader("Authorization") String bearer) {
        return ResponseEntity.ok(authService.verify(bearer.substring(7)));
    }
}

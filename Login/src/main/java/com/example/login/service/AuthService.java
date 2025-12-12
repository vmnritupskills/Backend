package com.example.login.service;

import com.example.login.dto.auth.*;
import com.example.login.entity.*;
import com.example.login.repository.*;
import com.example.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;
    private final PasswordEncoder passwordEncoder;

    // ------------------------- REGISTER ADMIN -------------------------
    @Transactional
    public AuthResp registerAdmin(RegisterAdminReq req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing"));

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roleId(adminRole.getId())
                .institutionId(null)
                .active(true)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);
        auditLogger.log("REGISTER_ADMIN", req.getEmail(), "Super Admin created");

        return generateLoginResponse(user, "ADMIN", null);
    }

    // ------------------------- REGISTER STUDENT -------------------------
    @Transactional
    public AuthResp registerStudent(RegisterStudentReq req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException("STUDENT role missing"));

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roleId(role.getId())
                .institutionId(req.getInstitutionId())
                .active(false)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);
        auditLogger.log("REGISTER_STUDENT", req.getEmail(), "Student pending approval");

        return new AuthResp(null, null, user.getEmail(), "STUDENT");
    }

    // ------------------------------ LOGIN ------------------------------
    @Transactional
    public AuthResp login(LoginReq req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("User is not active");
        }

        String roleName = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("UNKNOWN");

        // Student: enforce single device sessions
        if (roleName.equals("STUDENT")) {
            List<Session> activeSessions =
                    sessionRepository.findByUserIdAndActive(user.getId(), true);

            for (Session s : activeSessions) {
                s.setActive(false);
                sessionRepository.save(s);
            }
        }

        auditLogger.log("LOGIN", user.getEmail(), "User logged in");

        return generateLoginResponse(user, roleName, req.getDeviceInfo());
    }

    // ------------------ Generate Tokens + Save Session ------------------
    private AuthResp generateLoginResponse(User user, String roleName, Object deviceInfo) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleName);

        if (user.getInstitutionId() != null)
            claims.put("institutionId", user.getInstitutionId());

        String accessToken = jwtUtil.createAccessToken(user.getId().toString(), claims);
        String refreshToken = jwtUtil.createRefreshToken(user.getId().toString());

        // SESSION (ID auto-generated)
        Session session = Session.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .refreshToken(refreshToken)
                .deviceInfo(deviceInfo != null ? deviceInfo.toString() : null)
                .expiresAt(Instant.now().plusMillis(3600_000))
                .active(true)
                .createdAt(Instant.now())
                .build();

        sessionRepository.save(session);

        // REFRESH TOKEN (ID auto-generated)
        RefreshToken ref = RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .sessionId(session.getId())
                .expiresAt(Instant.now().plusMillis(7 * 24 * 3600_000))
                .isUsed(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(ref);

        return new AuthResp(accessToken, refreshToken, user.getEmail(), roleName);
    }

    // ----------------------------- LOGOUT -----------------------------
    @Transactional
    public void logout(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session"));

        session.setActive(false);
        sessionRepository.save(session);

        auditLogger.log("LOGOUT", session.getUserId().toString(), "Session ended");
    }

    // ---------------------- Refresh Token Rotation ----------------------
    @Transactional
    public AuthResp refresh(String refreshToken) {

        RefreshToken old = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (old.isUsed() || old.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired or already used");
        }

        old.setUsed(true);
        refreshTokenRepository.save(old);

        String newRefresh = jwtUtil.createRefreshToken(old.getUserId().toString());

        // Save new refresh token
        RefreshToken next = RefreshToken.builder()
                .token(newRefresh)
                .userId(old.getUserId())
                .sessionId(old.getSessionId())
                .expiresAt(Instant.now().plusMillis(7 * 24 * 3600_000))
                .isUsed(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(next);

        String newAccess = jwtUtil.createAccessToken(
                old.getUserId().toString(),
                Map.of()
        );

        auditLogger.log("REFRESH_TOKEN", old.getUserId().toString(), "Token rotated");

        return new AuthResp(newAccess, newRefresh, old.getUserId().toString(), null);
    }

    // ------------------------------ VERIFY ------------------------------
    public VerifyResp verify(String token) {
        var claims = jwtUtil.parseToken(token);
        VerifyResp resp = new VerifyResp();
        resp.setSubject(claims.getSubject());
        resp.setClaims(claims);
        return resp;
    }
}

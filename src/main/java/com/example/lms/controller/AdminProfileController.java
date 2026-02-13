package com.example.lms.controller;

import com.example.lms.dto.ChangePasswordDTO;
import com.example.lms.dto.ProfileResponseDTO;
import com.example.lms.dto.UpdateProfileDTO;
import com.example.lms.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    private final ProfileService profileService;

    /* ================= GET PROFILE ================= */
    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    /* ================= UPDATE PROFILE ================= */
    @PutMapping
    public ResponseEntity<String> updateProfile(
            @RequestBody @Valid UpdateProfileDTO dto) {

        profileService.updateProfile(dto);
        return ResponseEntity.ok("Profile updated successfully");
    }

    /* ================= CHANGE PASSWORD ================= */
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody @Valid ChangePasswordDTO dto) {

        profileService.changePassword(dto);
        return ResponseEntity.ok("Password changed successfully");
    }
}

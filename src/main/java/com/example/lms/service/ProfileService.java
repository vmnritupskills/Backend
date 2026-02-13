package com.example.lms.service;

import com.example.lms.dto.ChangePasswordDTO;
import com.example.lms.dto.ProfileResponseDTO;
import com.example.lms.dto.UpdateProfileDTO;
import com.example.lms.entity.User;
import com.example.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /* ================= GET PROFILE ================= */
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfile() {

        User user = getCurrentUser();

        return new ProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }

    /* ================= UPDATE PROFILE ================= */
    @Transactional
    public void updateProfile(UpdateProfileDTO dto) {

        User user = getCurrentUser();
        user.setName(dto.name());

        userRepository.save(user);
    }

    /* ================= CHANGE PASSWORD ================= */
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {

        User user = getCurrentUser();

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }
}

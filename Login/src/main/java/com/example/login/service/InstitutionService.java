package com.example.login.service;

import com.example.login.dto.institution.CreateInstitutionReq;
import com.example.login.dto.institution.InstitutionResp;
import com.example.login.entity.Institution;
import com.example.login.entity.Role;
import com.example.login.entity.User;
import com.example.login.repository.InstitutionRepository;
import com.example.login.repository.RoleRepository;
import com.example.login.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public InstitutionResp createInstitution(CreateInstitutionReq req, Long adminId) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Institution email already exists");
        }

        Role instRole = roleRepository.findByName("INSTITUTION_ADMIN")
                .orElseThrow(() -> new IllegalStateException("INSTITUTION_ADMIN role missing"));

        // 1️⃣ Create Institution
        Institution inst = Institution.builder()
                .name(req.getName())
                .email(req.getEmail())
                .active(true)
                .createdBy(adminId)
                .build();

        institutionRepository.save(inst);

        // 2️⃣ Create login user for institution admin
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roleId(instRole.getId())
                .institutionId(Long.valueOf(String.valueOf(inst.getId()))) // store institutionId as String
                .active(true)
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        return new InstitutionResp(inst.getId(), inst.getName(), inst.getEmail());
    }
}

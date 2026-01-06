package com.example.lms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lms.dto.CreateStudentRequestDTO;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final InstitutionRepository institutionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    /* ================= CREATE STUDENT ================= */
    @Transactional
    public void createStudent(CreateStudentRequestDTO dto) {

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Institution not found")
                );

        if (studentRepository
                .findByRegNoAndInstitution_Id(dto.regNo(), institution.getId())
                .isPresent()) {
            throw new IllegalStateException("Student already exists with this RegNo");
        }

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() ->
                        new RuntimeException("STUDENT role not found")
                );

        User user = User.builder()
                .email(dto.email())
                .name(dto.name())
                .password(passwordEncoder.encode(dto.password()))
                .role(studentRole)
                .isActive(true)
                .build();

        userRepository.save(user);

        Student student = Student.builder()
                .regNo(dto.regNo())
                .name(dto.name())
                .email(dto.email())
                .graduationYear(dto.graduationYear())
                .department(dto.department())
                .user(user)
                .institution(institution)
                .build();

        studentRepository.save(student);
    }
}

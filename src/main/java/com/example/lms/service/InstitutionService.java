package com.example.lms.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lms.dto.CreateInstitutionRequestDTO;
import com.example.lms.dto.UpdateInstitutionRequestDTO;
import com.example.lms.dto.InstitutionResponseDTO;
import com.example.lms.entity.Institution;
import com.example.lms.entity.Role;
import com.example.lms.entity.User;
import com.example.lms.exception.BadRequestException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.InstitutionRepository;
import com.example.lms.repository.RoleRepository;
import com.example.lms.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;


    /* ================= CREATE ================= */
    @Transactional
    public void createInstitution(CreateInstitutionRequestDTO dto) {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByName("INSTITUTION")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = User.builder()
                .email(dto.email())
                .name(dto.name())
                .password(passwordEncoder.encode(dto.password()))
                .role(role)
                .isActive(true)
                .build();

        userRepository.save(user);

        Institution institution = Institution.builder()
                .name(dto.name())
                .address(dto.address())
                .aisheCode(dto.aisheCode())
                .user(user)
                .build();

        institutionRepository.save(institution);

    }

    /* ================= GET ALL ================= */
    @Transactional(readOnly = true)
    public List<InstitutionResponseDTO> getAllInstitutions() {

        return institutionRepository.findByDeletedAtIsNull()
                .stream()
                .map(inst -> new InstitutionResponseDTO(
                        inst.getId(),
                        inst.getName(),
                        inst.getAddress(),
                        inst.getAisheCode(),
                        inst.getUser().getId(),
                        inst.getUser().getIsActive(),
                        inst.getCreatedAt()
                ))
                .toList();
    }

    /* ================= GET BY ID ================= */
    @Transactional(readOnly = true)
    public InstitutionResponseDTO getInstitutionById(Long id) {

        Institution inst = institutionRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        return new InstitutionResponseDTO(
                inst.getId(),
                inst.getName(),
                inst.getAddress(),
                inst.getAisheCode(),
                inst.getUser().getId(),
                inst.getUser().getIsActive(),
                inst.getCreatedAt()
        );
    }

    /* ================= UPDATE ================= */
    @Transactional
    public void updateInstitution(Long id, UpdateInstitutionRequestDTO dto) {

        Institution inst = institutionRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        if (dto.name() != null) inst.setName(dto.name());
        if (dto.address() != null) inst.setAddress(dto.address());
        if (dto.aisheCode() != null) inst.setAisheCode(dto.aisheCode());
        if (dto.isActive() != null) inst.getUser().setIsActive(dto.isActive());

        institutionRepository.save(inst);
    }

    /* ================= SOFT DELETE ================= */
    @Transactional
    public void deleteInstitution(Long id) {

        Institution inst = institutionRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        inst.setDeletedAt(LocalDateTime.now());
        inst.getUser().setIsActive(false);

        institutionRepository.save(inst);
    }
}

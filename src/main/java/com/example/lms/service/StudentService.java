package com.example.lms.service;

import com.example.lms.dto.CreateStudentRequestDTO;
import com.example.lms.dto.UpdateStudentRequestDTO;
import com.example.lms.dto.StudentResponseDTO;
import com.example.lms.entity.*;
import com.example.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final InstitutionRepository institutionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    /* ================= CREATE ================= */
    @Transactional
    public void createStudent(CreateStudentRequestDTO dto) {

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        if (studentRepository
                .findByRegNoAndInstitution_Id(dto.regNo(), institution.getId())
                .isPresent()) {
            throw new IllegalStateException("Student already exists with this RegNo");
        }

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

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

    /* ================= GET ALL ================= */
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /* ================= GET BY ID ================= */
    public StudentResponseDTO getStudentById(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return mapToDTO(student);
    }

    /* ================= UPDATE ================= */
    @Transactional
    public void updateStudent(Long id, UpdateStudentRequestDTO dto) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Institution institution = institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        student.setRegNo(dto.regNo());
        student.setName(dto.name());
        student.setEmail(dto.email());
        student.setDepartment(dto.department());
        student.setGraduationYear(dto.graduationYear());
        student.setInstitution(institution);

        User user = student.getUser();
        user.setName(dto.name());
        user.setEmail(dto.email());

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        userRepository.save(user);
        studentRepository.save(student);
    }

    /* ================= DELETE ================= */
    @Transactional
    public void deleteStudent(Long id) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        userRepository.delete(student.getUser());
        studentRepository.delete(student);
    }

    /* ================= MAPPER ================= */
    private StudentResponseDTO mapToDTO(Student student) {

        return new StudentResponseDTO(
                student.getId(),
                student.getRegNo(),
                student.getName(),
                student.getEmail(),
                student.getGraduationYear(),
                student.getDepartment(),
                student.getInstitution().getId()
        );
    }
}

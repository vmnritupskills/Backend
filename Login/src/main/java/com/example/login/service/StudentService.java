package com.example.login.service;

import com.example.login.dto.student.*;
import com.example.login.entity.Student;
import com.example.login.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    // ---------------- CREATE STUDENT ----------------
    @Transactional
    public StudentResp create(StudentReq req, String performedBy) {

        if (studentRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Student s = Student.builder()
                .name(req.getName())
                .email(req.getEmail())
                .rollNumber(req.getRollNumber())
                .branch(req.getBranch())
                .dateOfAdmission(req.getDateOfAdmission())
                .batchId(null)  // batch assigned later
                .build();

        studentRepository.save(s);

        return toResp(s);
    }

    // ---------------- BULK CREATE ----------------
    @Transactional
    public List<StudentResp> bulkCreate(BulkStudentReq req, String performedBy) {

        List<Student> list = new ArrayList<>();

        for (StudentReq r : req.getStudents()) {

            if (studentRepository.existsByEmail(r.getEmail())) {
                continue;
            }

            Student s = Student.builder()
                    .name(r.getName())
                    .email(r.getEmail())
                    .rollNumber(r.getRollNumber())
                    .branch(r.getBranch())
                    .dateOfAdmission(r.getDateOfAdmission())
                    .batchId(null)
                    .build();

            list.add(s);
        }

        studentRepository.saveAll(list);

        return list.stream().map(this::toResp).collect(Collectors.toList());
    }

    // ---------------- LIST STUDENTS ----------------
    public Map<String, Object> list(Long batchId, int page, int limit, String search) {

        List<Student> all = studentRepository.findAll();

        List<Student> filtered = all.stream()
                .filter(s -> {

                    boolean ok = true;

                    // filter by batch
                    if (batchId != null) {
                        ok = s.getBatchId() != null && s.getBatchId().equals(batchId);
                    }

                    // filter by search (name or email)
                    if (ok && search != null && !search.isBlank()) {
                        ok =
                                (s.getName() != null && s.getName().toLowerCase().contains(search.toLowerCase()))
                                        ||
                                        (s.getEmail() != null && s.getEmail().toLowerCase().contains(search.toLowerCase()));
                    }

                    return ok;
                })
                .collect(Collectors.toList());

        int from = (page - 1) * limit;
        int to = Math.min(from + limit, filtered.size());

        List<StudentResp> pageList =
                (from >= filtered.size())
                        ? Collections.emptyList()
                        : filtered.subList(from, to)
                        .stream().map(this::toResp).collect(Collectors.toList());

        return Map.of(
                "total", filtered.size(),
                "page", page,
                "limit", limit,
                "data", pageList
        );
    }

    // ---------------- UPDATE STUDENT ----------------
    @Transactional
    public StudentResp update(Long id, StudentReq req, String performedBy) {

        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        s.setName(req.getName());
        s.setEmail(req.getEmail());
        s.setRollNumber(req.getRollNumber());
        s.setBranch(req.getBranch());
        s.setDateOfAdmission(req.getDateOfAdmission());

        studentRepository.save(s);

        return toResp(s);
    }

    // ---------------- ASSIGN BATCH ----------------
    @Transactional
    public StudentResp assignBatch(Long id, Long batchId, String performedBy) {

        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        s.setBatchId(batchId);
        studentRepository.save(s);

        return toResp(s);
    }

    // ---------------- CONVERT ENTITY → DTO ----------------
    private StudentResp toResp(Student s) {
        return StudentResp.builder()
                .id(s.getId())
                .name(s.getName())
                .email(s.getEmail())
                .batchId(s.getBatchId())
                .rollNumber(s.getRollNumber())
                .branch(s.getBranch())
                .dateOfAdmission(s.getDateOfAdmission())
                .build();
    }
}
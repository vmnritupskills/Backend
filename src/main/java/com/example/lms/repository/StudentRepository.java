package com.example.lms.repository;

import com.example.lms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {


    Optional<Student> findByRegNoAndInstitution_Id(String regNo, Long institutionId);


    List<Student> findByRegNoInAndInstitution_Id(List<String> regNos, Long institutionId);
}

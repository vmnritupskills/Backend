package com.example.lms.repository;

import com.example.lms.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByAssignment_Id(Long assignmentId);
}

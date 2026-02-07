package com.example.lms.repository;

import com.example.lms.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTopic_Id(Long topicId);


    List<Assignment> findByTopic_Course_IdIn(List<Long> courseIds);

}

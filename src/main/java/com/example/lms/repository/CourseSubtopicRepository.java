package com.example.lms.repository;

import com.example.lms.entity.CourseSubtopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSubtopicRepository
        extends JpaRepository<CourseSubtopic, Long> {

    List<CourseSubtopic> findByTopicId(Long topicId);
}

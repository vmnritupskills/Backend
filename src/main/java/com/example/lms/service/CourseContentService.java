package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseContentService {

    private final CourseRepository courseRepo;
    private final CourseTopicRepository topicRepo;
    private final CourseSubtopicRepository subtopicRepo;
    private final InstitutionCourseManagerRepository icmRepo;
    private final S3Service s3Service;

    /* ================= TOPIC ================= */

    public CourseTopic createTopic(Long cmId, CreateTopicDTO dto) {

        if (!icmRepo.existsByCourseIdAndContentManagerId(dto.courseId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        Course course = courseRepo.findById(dto.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        return topicRepo.save(
                CourseTopic.builder()
                        .title(dto.title())
                        .durationMinutes(dto.durationMinutes())
                        .course(course)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public List<CourseTopic> getTopicsByCourse(Long courseId) {
        return topicRepo.findByCourseId(courseId);
    }

    public CourseTopic updateTopic(Long topicId, UpdateTopicDTO dto) {

        CourseTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        topic.setTitle(dto.title());
        topic.setDurationMinutes(dto.durationMinutes());

        return topicRepo.save(topic);
    }

    public void deleteTopic(Long cmId, Long topicId) {

        CourseTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                topic.getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        topicRepo.delete(topic);
    }

    /* ================= SUBTOPIC ================= */

    public CourseSubtopic createSubtopic(
            Long cmId,
            CreateSubtopicDTO dto,
            MultipartFile file
    ) {

        // ✅ FIXED: use getters (DTO is a CLASS, not record)
        CourseTopic topic = topicRepo.findById(dto.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                topic.getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        String contentUrl = null;
        String textContent = null;

        // 📁 FILE CONTENT
        if (dto.getContentType() == ContentType.PDF ||
                dto.getContentType() == ContentType.VIDEO) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File required");
            }

            contentUrl = s3Service.uploadFile(file, "course-content");
        }

        // 📝 TEXT CONTENT
        if (dto.getContentType() == ContentType.TEXT) {
            if (dto.getTextContent() == null || dto.getTextContent().isBlank()) {
                throw new IllegalArgumentException("Text content required");
            }
            textContent = dto.getTextContent();
        }

        return subtopicRepo.save(
                CourseSubtopic.builder()
                        .title(dto.getTitle())
                        .contentType(dto.getContentType())
                        .contentUrl(contentUrl)
                        .textContent(textContent)
                        .durationMinutes(dto.getDurationMinutes())
                        .topic(topic)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public List<CourseSubtopic> getSubtopicsByTopic(Long topicId) {
        return subtopicRepo.findByTopicId(topicId);
    }

    public CourseSubtopic updateSubtopic(
            Long subtopicId,
            UpdateSubtopicDTO dto,
            MultipartFile file
    ) {

        CourseSubtopic subtopic = subtopicRepo.findById(subtopicId)
                .orElseThrow(() -> new IllegalArgumentException("Subtopic not found"));

        subtopic.setTitle(dto.title());
        subtopic.setDurationMinutes(dto.durationMinutes());
        subtopic.setContentType(dto.contentType());

        if (dto.contentType() == ContentType.TEXT) {
            subtopic.setTextContent(dto.textContent());
            subtopic.setContentUrl(null);
        }

        if ((dto.contentType() == ContentType.PDF ||
                dto.contentType() == ContentType.VIDEO) && file != null) {

            subtopic.setContentUrl(
                    s3Service.uploadFile(file, "course-content")
            );
            subtopic.setTextContent(null);
        }

        return subtopicRepo.save(subtopic);
    }

    public void deleteSubtopic(Long subtopicId) {
        subtopicRepo.deleteById(subtopicId);
    }

    /* ================= COURSE TREE ================= */

    @Transactional(readOnly = true)
    public CourseContentResponseDTO getCourseContentStructure(
            Long cmId,
            Long courseId
    ) {

        if (!icmRepo.existsByCourseIdAndContentManagerId(courseId, cmId)) {
            throw new SecurityException("Access denied");
        }

        List<CourseTopic> topics = topicRepo.findByCourseId(courseId);

        List<TopicWithSubtopicsDTO> topicDtos =
                topics.stream()
                        .map(topic -> new TopicWithSubtopicsDTO(
                                topic.getId(),
                                topic.getTitle(),
                                topic.getDurationMinutes(),
                                subtopicRepo.findByTopicId(topic.getId())
                                        .stream()
                                        .map(sub -> new SubtopicResponseDTO(
                                                sub.getId(),
                                                sub.getTitle(),
                                                sub.getContentType(),
                                                sub.getContentUrl(),
                                                sub.getTextContent(),
                                                sub.getDurationMinutes()
                                        ))
                                        .toList()
                        ))
                        .toList();

        return new CourseContentResponseDTO(courseId, topicDtos);
    }
}

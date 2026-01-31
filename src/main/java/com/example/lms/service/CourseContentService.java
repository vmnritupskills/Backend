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
    private final QuizRepository quizRepo;
    private final S3Service s3Service;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizAttemptRepository attemptRepo;


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

        CourseTopic topic = topicRepo.findById(dto.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                topic.getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        String contentUrl = null;
        String textContent = null;

        if (dto.getContentType() == ContentType.PDF ||
                dto.getContentType() == ContentType.VIDEO) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File required");
            }

            contentUrl = s3Service.uploadFile(file, "course-content");
        }

        if (dto.getContentType() == ContentType.TEXT) {
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

    /* ================= COURSE STRUCTURE ================= */

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

                                // SUBTOPICS
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
                                        .toList(),

                                // QUIZZES
                                quizRepo.findByTopicId(topic.getId())
                                        .stream()
                                        .map(q -> new QuizStatusDTO(
                                                q.getId(),
                                                q.getTitle(),
                                                q.getIsActive(),
                                                q.getPassMarks()
                                        ))
                                        .toList()
                        ))
                        .toList();

        return new CourseContentResponseDTO(courseId, topicDtos);
    }

    @Transactional(readOnly = true)
    public CourseCompletionStatsDTO getAverageCourseCompletion(Long cmId, Long courseId) {

        // CM authorization
        if (!icmRepo.existsByCourseIdAndContentManagerId(courseId, cmId)) {
            throw new SecurityException("Access denied");
        }

        List<CourseTopic> topics = topicRepo.findByCourseId(courseId);
        int totalTopics = topics.size();

        if (totalTopics == 0) {
            return new CourseCompletionStatsDTO(courseId, 0.0, 0);
        }

        List<Enrollment> enrollments =
                enrollmentRepository.findByCourseIdAndIsEnrolledTrue(courseId);

        if (enrollments.isEmpty()) {
            return new CourseCompletionStatsDTO(courseId, 0.0, 0);
        }

        double totalCompletionSum = 0;

        for (Enrollment enrollment : enrollments) {
            Long studentId = enrollment.getStudent().getId();
            int completedTopics = 0;

            for (CourseTopic topic : topics) {
                List<Quiz> quizzes = quizRepo.findByTopicId(topic.getId());

                if (quizzes.isEmpty()) continue;

                Quiz quiz = quizzes.get(0); // one quiz per topic (your design)

                boolean passed =
                        attemptRepo.existsByQuizIdAndStudentIdAndPassedTrue(
                                quiz.getId(), studentId
                        );

                if (passed) {
                    completedTopics++;
                }
            }

            double studentCompletion =
                    ((double) completedTopics / totalTopics) * 100;

            totalCompletionSum += studentCompletion;
        }

        double averageCompletion =
                totalCompletionSum / enrollments.size();

        return new CourseCompletionStatsDTO(
                courseId,
                Math.round(averageCompletion * 100.0) / 100.0, // 2 decimals
                enrollments.size()
        );
    }

}

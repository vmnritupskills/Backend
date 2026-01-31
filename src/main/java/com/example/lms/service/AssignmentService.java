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
public class AssignmentService {

    private final AssignmentRepository assignmentRepo;
    private final AssignmentSubmissionRepository submissionRepo;
    private final CourseTopicRepository topicRepo;
    private final InstitutionCourseManagerRepository icmRepo;
    private final S3Service s3Service;

    /* ================= CREATE ================= */

    public AssignmentResponseDTO createAssignment(
            Long cmId,
            CreateAssignmentDTO dto,
            MultipartFile file
    ) {

        CourseTopic topic = topicRepo.findById(dto.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                topic.getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Assignment file required");
        }

        String fileUrl = s3Service.uploadFile(file, "assignments");

        Assignment saved = assignmentRepo.save(
                Assignment.builder()
                        .title(dto.title())
                        .questionFileUrl(fileUrl)
                        .deadline(dto.deadline())
                        .topic(topic)
                        .build()
        );

        return toDTO(saved);
    }

    /* ================= GET ================= */

    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getAssignmentsByTopic(
            Long cmId,
            Long topicId
    ) {

        CourseTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                topic.getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        return assignmentRepo.findByTopic_Id(topicId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* ================= UPDATE ================= */

    public AssignmentResponseDTO updateAssignment(
            Long cmId,
            Long assignmentId,
            CreateAssignmentDTO dto,
            MultipartFile file
    ) {

        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                assignment.getTopic().getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        assignment.setTitle(dto.title());
        assignment.setDeadline(dto.deadline());

        if (file != null && !file.isEmpty()) {
            assignment.setQuestionFileUrl(
                    s3Service.uploadFile(file, "assignments")
            );
        }

        return toDTO(assignmentRepo.save(assignment));
    }

    /* ================= DELETE ================= */

    public void deleteAssignment(Long cmId, Long assignmentId) {

        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                assignment.getTopic().getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        assignmentRepo.delete(assignment);
    }

    /* ================= SUBMISSIONS (CM VIEW) ================= */

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponseDTO> getSubmissions(
            Long cmId,
            Long assignmentId
    ) {

        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        if (!icmRepo.existsByCourseIdAndContentManagerId(
                assignment.getTopic().getCourse().getId(), cmId)) {
            throw new SecurityException("Access denied");
        }

        return submissionRepo.findByAssignment_Id(assignmentId)
                .stream()
                .map(s -> new AssignmentSubmissionResponseDTO(
                        s.getId(),
                        s.getStudentId(),
                        s.getSubmissionFileUrl(),
                        s.getSubmittedAt(),
                        s.getMarks()
                ))
                .toList();
    }

    /* ================= MAPPER ================= */

    private AssignmentResponseDTO toDTO(Assignment a) {
        return new AssignmentResponseDTO(
                a.getId(),
                a.getTitle(),
                a.getQuestionFileUrl(),
                a.getDeadline()
        );
    }
}

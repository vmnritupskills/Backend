package com.example.lms.service;

import com.example.lms.dto.AssignmentResponseDTO;
import com.example.lms.dto.AssignmentSubmissionResponseDTO;
import com.example.lms.dto.CreateAssignmentDTO;
import com.example.lms.entity.Assignment;
import com.example.lms.entity.AssignmentSubmission;
import com.example.lms.entity.Course;
import com.example.lms.entity.CourseTopic;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.AssignmentSubmissionRepository;
import com.example.lms.repository.CourseTopicRepository;
import com.example.lms.repository.InstitutionCourseManagerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepo;

    @Mock
    private AssignmentSubmissionRepository submissionRepo;

    @Mock
    private CourseTopicRepository topicRepo;

    @Mock
    private InstitutionCourseManagerRepository icmRepo;

    @Mock
    private S3Service s3Service;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private AssignmentService assignmentService;

    /* ================= HELPER ================= */

    private CourseTopic mockTopic() {
        Course course = Course.builder().id(1L).build();
        return CourseTopic.builder().id(10L).course(course).build();
    }

    /* ================= CREATE ================= */

    @Test
    void createAssignment_success() {

        CourseTopic topic = mockTopic();

        CreateAssignmentDTO dto = new CreateAssignmentDTO(
                topic.getId(),
                "Assignment 1",
                LocalDateTime.now().plusDays(5)
        );

        when(topicRepo.findById(topic.getId()))
                .thenReturn(Optional.of(topic));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 100L))
                .thenReturn(true);

        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(file, "assignments"))
                .thenReturn("s3-url");

        when(assignmentRepo.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        AssignmentResponseDTO response =
                assignmentService.createAssignment(100L, dto, file);

        assertNotNull(response);
        assertEquals("Assignment 1", response.title());
        assertEquals("s3-url", response.questionFileUrl());
    }

    @Test
    void createAssignment_fail_topic_not_found() {

        CreateAssignmentDTO dto = new CreateAssignmentDTO(
                99L,
                "A1",
                LocalDateTime.now()
        );

        when(topicRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.createAssignment(1L, dto, file)
        );
    }

    @Test
    void createAssignment_fail_access_denied() {

        CourseTopic topic = mockTopic();

        CreateAssignmentDTO dto = new CreateAssignmentDTO(
                topic.getId(),
                "A1",
                LocalDateTime.now()
        );

        when(topicRepo.findById(topic.getId()))
                .thenReturn(Optional.of(topic));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(false);

        assertThrows(
                SecurityException.class,
                () -> assignmentService.createAssignment(1L, dto, file)
        );
    }

    @Test
    void createAssignment_fail_file_missing() {

        CourseTopic topic = mockTopic();

        CreateAssignmentDTO dto = new CreateAssignmentDTO(
                topic.getId(),
                "A1",
                LocalDateTime.now()
        );

        when(topicRepo.findById(topic.getId()))
                .thenReturn(Optional.of(topic));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.createAssignment(1L, dto, null)
        );
    }

    /* ================= GET BY TOPIC ================= */

    @Test
    void getAssignmentsByTopic_success() {

        CourseTopic topic = mockTopic();

        Assignment assignment =
                Assignment.builder().id(1L).topic(topic).build();

        when(topicRepo.findById(topic.getId()))
                .thenReturn(Optional.of(topic));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(true);

        when(assignmentRepo.findByTopic_Id(topic.getId()))
                .thenReturn(List.of(assignment));

        List<AssignmentResponseDTO> list =
                assignmentService.getAssignmentsByTopic(1L, topic.getId());

        assertEquals(1, list.size());
    }

    @Test
    void getAssignmentsByTopic_fail_access_denied() {

        CourseTopic topic = mockTopic();

        when(topicRepo.findById(topic.getId()))
                .thenReturn(Optional.of(topic));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(false);

        assertThrows(
                SecurityException.class,
                () -> assignmentService.getAssignmentsByTopic(1L, topic.getId())
        );
    }

    /* ================= GET ALL ================= */

    @Test
    void getAllAssignments_success() {

        when(icmRepo.findCourseIdsByContentManagerId(1L))
                .thenReturn(List.of(1L));

        when(assignmentRepo.findByTopic_Course_IdIn(List.of(1L)))
                .thenReturn(List.of(Assignment.builder().id(1L).build()));

        List<AssignmentResponseDTO> list =
                assignmentService.getAllAssignments(1L);

        assertEquals(1, list.size());
    }

    @Test
    void getAllAssignments_no_courses() {

        when(icmRepo.findCourseIdsByContentManagerId(1L))
                .thenReturn(List.of());

        List<AssignmentResponseDTO> list =
                assignmentService.getAllAssignments(1L);

        assertTrue(list.isEmpty());
        verify(assignmentRepo, never()).findByTopic_Course_IdIn(any());
    }

    /* ================= UPDATE ================= */

    @Test
    void updateAssignment_success_with_file() {

        CourseTopic topic = mockTopic();

        Assignment assignment =
                Assignment.builder().id(1L).topic(topic).build();

        CreateAssignmentDTO dto = new CreateAssignmentDTO(
                topic.getId(),
                "Updated",
                LocalDateTime.now().plusDays(3)
        );

        when(assignmentRepo.findById(1L))
                .thenReturn(Optional.of(assignment));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(true);

        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(file, "assignments"))
                .thenReturn("new-url");

        when(assignmentRepo.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        AssignmentResponseDTO response =
                assignmentService.updateAssignment(1L, 1L, dto, file);

        assertEquals("Updated", response.title());
        assertEquals("new-url", response.questionFileUrl());
    }

    /* ================= DELETE ================= */

    @Test
    void deleteAssignment_success() {

        CourseTopic topic = mockTopic();

        Assignment assignment =
                Assignment.builder().id(1L).topic(topic).build();

        when(assignmentRepo.findById(1L))
                .thenReturn(Optional.of(assignment));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(true);

        assignmentService.deleteAssignment(1L, 1L);

        verify(assignmentRepo).delete(assignment);
    }

    /* ================= SUBMISSIONS ================= */

    @Test
    void getSubmissions_success() {

        CourseTopic topic = mockTopic();

        Assignment assignment =
                Assignment.builder().id(1L).topic(topic).build();

        AssignmentSubmission submission =
                AssignmentSubmission.builder()
                        .id(1L)
                        .studentId(101L)
                        .submissionFileUrl("url")
                        .build();

        when(assignmentRepo.findById(1L))
                .thenReturn(Optional.of(assignment));

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L))
                .thenReturn(true);

        when(submissionRepo.findByAssignment_Id(1L))
                .thenReturn(List.of(submission));

        List<AssignmentSubmissionResponseDTO> list =
                assignmentService.getSubmissions(1L, 1L);

        assertEquals(1, list.size());
        assertEquals(101L, list.get(0).studentId());
    }
}

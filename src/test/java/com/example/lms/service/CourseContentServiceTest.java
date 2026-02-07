package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseContentService Test Suite")
public class CourseContentServiceTest {

    @Mock
    private CourseRepository courseRepo;

    @Mock
    private CourseTopicRepository topicRepo;

    @Mock
    private CourseSubtopicRepository subtopicRepo;

    @Mock
    private InstitutionCourseManagerRepository icmRepo;

    @Mock
    private QuizRepository quizRepo;

    @Mock
    private S3Service s3Service;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private QuizAttemptRepository attemptRepo;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CourseContentService service;

    private Course course;
    private CourseTopic topic;
    private CourseSubtopic subtopic;
    private CreateTopicDTO createTopicDTO;
    private UpdateTopicDTO updateTopicDTO;
    private CreateSubtopicDTO createSubtopicDTO;
    private UpdateSubtopicDTO updateSubtopicDTO;

    @BeforeEach
    void setUp() {
        // Initialize course
        course = new Course();
        course.setId(1L);
        course.setName("Java Basics");

        // Initialize topic
        topic = new CourseTopic();
        topic.setId(1L);
        topic.setTitle("Arrays");
        topic.setDurationMinutes(60);
        topic.setCourse(course);

        // Initialize subtopic
        subtopic = new CourseSubtopic();
        subtopic.setId(1L);
        subtopic.setTitle("Array Basics");
        subtopic.setContentType(ContentType.VIDEO);
        subtopic.setContentUrl("https://s3.example.com/video1.mp4");
        subtopic.setDurationMinutes(30);
        subtopic.setTopic(topic);

        // Initialize DTOs
        createTopicDTO = new CreateTopicDTO(1L, "Arrays", 60);
        updateTopicDTO = new UpdateTopicDTO("Arrays - Updated", 90);
        createSubtopicDTO = new CreateSubtopicDTO();
        createSubtopicDTO.setTopicId(1L);
        createSubtopicDTO.setTitle("Array Basics");
        createSubtopicDTO.setContentType(ContentType.TEXT);
        createSubtopicDTO.setTextContent("Array content here");
        createSubtopicDTO.setDurationMinutes(30);

        updateSubtopicDTO = new UpdateSubtopicDTO();
        updateSubtopicDTO.setTitle("Array Basics - Updated");
        updateSubtopicDTO.setContentType(ContentType.TEXT);
        updateSubtopicDTO.setTextContent("Updated content");
        updateSubtopicDTO.setDurationMinutes(45);
    }

    // ===== CREATE TOPIC TESTS =====

    @Test
    @DisplayName("Test 1: Should create topic successfully with valid access")
    void testCreateTopic_Success() {
        // Arrange
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
        when(topicRepo.save(any(CourseTopic.class))).thenReturn(topic);

        // Act
        CourseTopic result = service.createTopic(1L, createTopicDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Arrays");
        verify(courseRepo, times(1)).findById(1L);
        verify(topicRepo, times(1)).save(any(CourseTopic.class));
    }

    @Test
    @DisplayName("Test 2: Should throw SecurityException when user has no access to create topic")
    void testCreateTopic_AccessDenied() {
        // Arrange
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.createTopic(999L, createTopicDTO))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied");

        verify(courseRepo, never()).findById(any());
    }

    @Test
    @DisplayName("Test 3: Should throw exception when course not found during topic creation")
    void testCreateTopic_CourseNotFound() {
        // Arrange
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(courseRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.createTopic(1L, createTopicDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Course not found");

        verify(topicRepo, never()).save(any());
    }

    // ===== GET TOPICS TESTS =====

    @Test
    @DisplayName("Test 4: Should retrieve all topics for a course with valid access")
    void testGetTopicsByCourse_Success() {
        // Arrange
        CourseTopic topic2 = new CourseTopic();
        topic2.setId(2L);
        topic2.setTitle("Lists");

        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(topicRepo.findByCourseId(1L)).thenReturn(Arrays.asList(topic, topic2));

        // Act
        List<CourseTopic> results = service.getTopicsByCourse(1L, 1L);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("Arrays");
        assertThat(results.get(1).getTitle()).isEqualTo("Lists");
        verify(topicRepo, times(1)).findByCourseId(1L);
    }

    @Test
    @DisplayName("Test 5: Should throw SecurityException when retrieving topics without access")
    void testGetTopicsByCourse_AccessDenied() {
        // Arrange
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.getTopicsByCourse(999L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied");

        verify(topicRepo, never()).findByCourseId(any());
    }

    // ===== UPDATE TOPIC TESTS =====

    @Test
    @DisplayName("Test 6: Should update topic successfully with valid access")
    void testUpdateTopic_Success() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(topicRepo.save(any(CourseTopic.class))).thenReturn(topic);

        // Act
        CourseTopic result = service.updateTopic(1L, 1L, updateTopicDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(topicRepo, times(1)).findById(1L);
        verify(topicRepo, times(1)).save(any(CourseTopic.class));
    }

    @Test
    @DisplayName("Test 7: Should throw exception when topic not found during update")
    void testUpdateTopic_NotFound() {
        // Arrange
        when(topicRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateTopic(1L, 999L, updateTopicDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Topic not found");

        verify(topicRepo, never()).save(any());
    }

    @Test
    @DisplayName("Test 8: Should throw SecurityException when updating topic without access")
    void testUpdateTopic_AccessDenied() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.updateTopic(999L, 1L, updateTopicDTO))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied");

        verify(topicRepo, never()).save(any());
    }

    // ===== DELETE TOPIC TESTS =====

    @Test
    @DisplayName("Test 9: Should delete topic successfully with valid access")
    void testDeleteTopic_Success() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);

        // Act
        service.deleteTopic(1L, 1L);

        // Assert
        verify(topicRepo, times(1)).delete(topic);
    }

    @Test
    @DisplayName("Test 10: Should throw SecurityException when deleting topic without access")
    void testDeleteTopic_AccessDenied() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.deleteTopic(999L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied");

        verify(topicRepo, never()).delete(any());
    }

    // ===== CREATE SUBTOPIC TESTS =====

    @Test
    @DisplayName("Test 11: Should create text subtopic successfully")
    void testCreateSubtopic_TextContent_Success() {
        // Arrange
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(subtopicRepo.save(any(CourseSubtopic.class))).thenReturn(subtopic);

        // Act
        CourseSubtopic result = service.createSubtopic(1L, createSubtopicDTO, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(s3Service, never()).uploadFile(any(), any());
        verify(subtopicRepo, times(1)).save(any(CourseSubtopic.class));
    }

    @Test
    @DisplayName("Test 12: Should create video subtopic with file upload")
    void testCreateSubtopic_VideoFile_Success() {
        // Arrange
        createSubtopicDTO.setContentType(ContentType.VIDEO);
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(file, "course-content")).thenReturn("https://s3.example.com/video.mp4");
        when(subtopicRepo.save(any(CourseSubtopic.class))).thenReturn(subtopic);

        // Act
        CourseSubtopic result = service.createSubtopic(1L, createSubtopicDTO, file);

        // Assert
        assertThat(result).isNotNull();
        verify(s3Service, times(1)).uploadFile(file, "course-content");
        verify(subtopicRepo, times(1)).save(any(CourseSubtopic.class));
    }

    @Test
    @DisplayName("Test 13: Should throw exception when file required but not provided for video")
    void testCreateSubtopic_VideoFile_Missing() {
        // Arrange
        createSubtopicDTO.setContentType(ContentType.VIDEO);
        when(topicRepo.findById(1L)).thenReturn(Optional.of(topic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> service.createSubtopic(1L, createSubtopicDTO, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File required");

        verify(subtopicRepo, never()).save(any());
    }

    // ===== GET SUBTOPIC TESTS =====

    @Test
    @DisplayName("Test 14: Should retrieve all subtopics for a topic")
    void testGetSubtopicsByTopic_Success() {
        // Arrange
        CourseSubtopic subtopic2 = new CourseSubtopic();
        subtopic2.setId(2L);
        subtopic2.setTitle("Array Sorting");

        when(subtopicRepo.findByTopicId(1L)).thenReturn(Arrays.asList(subtopic, subtopic2));

        // Act
        List<CourseSubtopic> results = service.getSubtopicsByTopic(1L);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("Array Basics");
        assertThat(results.get(1).getTitle()).isEqualTo("Array Sorting");
        verify(subtopicRepo, times(1)).findByTopicId(1L);
    }

    // ===== UPDATE SUBTOPIC TESTS =====

    @Test
    @DisplayName("Test 15: Should update subtopic with text content")
    void testUpdateSubtopic_TextContent_Success() {
        // Arrange
        when(subtopicRepo.findById(1L)).thenReturn(Optional.of(subtopic));
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(subtopicRepo.save(any(CourseSubtopic.class))).thenReturn(subtopic);

        // Act
        CourseSubtopic result = service.updateSubtopic(1L, 1L, updateSubtopicDTO, null);

        // Assert
        assertThat(result).isNotNull();
        verify(subtopicRepo, times(1)).save(any(CourseSubtopic.class));
    }

    @Test
    @DisplayName("Test 16: Should throw exception when updating non-existent subtopic")
    void testUpdateSubtopic_NotFound() {
        // Arrange
        when(subtopicRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateSubtopic(1L, 999L, updateSubtopicDTO, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Subtopic not found");

        verify(subtopicRepo, never()).save(any());
    }

    // ===== DELETE SUBTOPIC TESTS =====

    @Test
    @DisplayName("Test 17: Should delete subtopic successfully")
    void testDeleteSubtopic_Success() {
        // Arrange
        doNothing().when(subtopicRepo).deleteById(1L);

        // Act
        service.deleteSubtopic(1L);

        // Assert
        verify(subtopicRepo, times(1)).deleteById(1L);
    }

    // ===== COURSE STRUCTURE TESTS =====

    @Test
    @DisplayName("Test 18: Should retrieve course content structure with valid access")
    void testGetCourseContentStructure_Success() {
        // Arrange
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 1L)).thenReturn(true);
        when(topicRepo.findByCourseId(1L)).thenReturn(Arrays.asList(topic));
        when(subtopicRepo.findByTopicId(1L)).thenReturn(Arrays.asList(subtopic));
        when(quizRepo.findByTopicId(1L)).thenReturn(List.of());

        // Act
        CourseContentResponseDTO result = service.getCourseContentStructure(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.courseId()).isEqualTo(1L);
        verify(topicRepo, times(1)).findByCourseId(1L);
    }

    @Test
    @DisplayName("Test 19: Should throw SecurityException when accessing structure without permission")
    void testGetCourseContentStructure_AccessDenied() {
        // Arrange
        when(icmRepo.existsByCourse_IdAndContentManager_Id(1L, 999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.getCourseContentStructure(999L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied");

        verify(topicRepo, never()).findByCourseId(any());
    }

    // ===== STREAM SUBTOPIC TESTS =====

    @Test
    @DisplayName("Test 20: Should stream subtopic content successfully")
    void testStreamSubtopicContent_Success() {
        // Arrange
        S3FileResponse fileResponse = new S3FileResponse(new byte[]{1, 2, 3}, "video.mp4", "video/mp4");
        when(subtopicRepo.findById(1L)).thenReturn(Optional.of(subtopic));
        when(s3Service.downloadFile("https://s3.example.com/video1.mp4")).thenReturn(fileResponse);

        // Act
        ResponseEntity<byte[]> result = service.streamSubtopicContent(1L);

        // Assert
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(new byte[]{1, 2, 3});
        verify(s3Service, times(1)).downloadFile(any());
    }
}

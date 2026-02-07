package com.example.lms.service;

import com.example.lms.dto.CreateCourseRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.entity.Institution;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.InstitutionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Tests")
class CourseServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private CourseService courseService;

    private Institution testInstitution;
    private Course testCourse;
    private CreateCourseRequestDTO createDTO;
    private MockMultipartFile testSyllabus;

    @BeforeEach
    void setUp() {
        testInstitution = Institution.builder()
                .id(1L)
                .name("Test Institution")
                .build();

        testCourse = Course.builder()
                .id(1L)
                .name("Computer Science")
                .courseCode("CS101")
                .duration("4 Years")
                .semester(1)
                .syllabusUrl("https://s3.amazonaws.com/syllabus.pdf")
                .institution(testInstitution)
                .build();

        testSyllabus = new MockMultipartFile(
                "syllabus",
                "test.pdf",
                "application/pdf",
                "syllabus content".getBytes()
        );

        createDTO = new CreateCourseRequestDTO(
                1L,
                "Computer Science",
                "CS101",
                "4 Years",
                1,
                testSyllabus
        );
    }

    @Nested
    @DisplayName("createCourse - Happy Path Tests")
    class CreateCourseHappyPath {

        @Test
        @DisplayName("Should successfully create a course with syllabus")
        void createCourse_withSyllabus_success() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(false);
            when(s3Service.uploadSyllabus(testSyllabus))
                    .thenReturn("https://s3.amazonaws.com/syllabus.pdf");

            // Act
            courseService.createCourse(createDTO, 1L);

            // Assert
            ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
            verify(courseRepository).save(courseCaptor.capture());
            Course savedCourse = courseCaptor.getValue();

            assertEquals("Computer Science", savedCourse.getName());
            assertEquals("CS101", savedCourse.getCourseCode());
            assertEquals("4 Years", savedCourse.getDuration());
            assertEquals(1, savedCourse.getSemester());
            assertEquals("https://s3.amazonaws.com/syllabus.pdf", savedCourse.getSyllabusUrl());
            assertEquals(testInstitution, savedCourse.getInstitution());
        }

        @Test
        @DisplayName("Should successfully create a course without syllabus")
        void createCourse_withoutSyllabus_success() {
            // Arrange
            CreateCourseRequestDTO dtoNoSyllabus = new CreateCourseRequestDTO(
                    1L, "Computer Science", "CS101", "4 Years", 1, null
            );

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(false);

            // Act
            courseService.createCourse(dtoNoSyllabus, 1L);

            // Assert
            ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
            verify(courseRepository).save(courseCaptor.capture());
            Course savedCourse = courseCaptor.getValue();

            assertNull(savedCourse.getSyllabusUrl());
            verify(s3Service, never()).uploadSyllabus(any());
        }

        @Test
        @DisplayName("Should upload syllabus to S3")
        void createCourse_uploadsSyllabus() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(false);
            when(s3Service.uploadSyllabus(testSyllabus))
                    .thenReturn("https://s3.amazonaws.com/syllabus.pdf");

            // Act
            courseService.createCourse(createDTO, 1L);

            // Assert
            verify(s3Service).uploadSyllabus(testSyllabus);
        }

        @Test
        @DisplayName("Should handle empty syllabus file")
        void createCourse_emptySyllabusFile_success() {
            // Arrange
            MockMultipartFile emptySyllabus = new MockMultipartFile(
                    "syllabus", "test.pdf", "application/pdf", new byte[0]
            );
            CreateCourseRequestDTO dtoEmptySyllabus = new CreateCourseRequestDTO(
                    1L, "Computer Science", "CS101", "4 Years", 1, emptySyllabus
            );

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(false);

            // Act
            courseService.createCourse(dtoEmptySyllabus, 1L);

            // Assert
            verify(s3Service, never()).uploadSyllabus(any());
        }
    }

    @Nested
    @DisplayName("createCourse - Unhappy Path Tests")
    class CreateCourseUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when institution not found")
        void createCourse_institutionNotFound_throwsException() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(999L))
                    .thenReturn(Optional.empty());

            CreateCourseRequestDTO dto = new CreateCourseRequestDTO(
                    999L, "CS", "CS101", "4 Years", 1, null
            );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.createCourse(dto, 999L)
            );

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when course code already exists")
        void createCourse_duplicateCourseCode_throwsException() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(
                    IllegalStateException.class,
                    () -> courseService.createCourse(createDTO, 1L)
            );

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not save course if institution validation fails")
        void createCourse_institutionValidationFails_doesNotSave() {
            // Arrange
            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.createCourse(createDTO, 1L)
            );

            verify(s3Service, never()).uploadSyllabus(any());
            verify(courseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllCourses - Happy Path Tests")
    class GetAllCoursesHappyPath {

        @Test
        @DisplayName("Should retrieve all courses for institution")
        void getAllCourses_success() {
            // Arrange
            Course course2 = Course.builder()
                    .id(2L)
                    .name("Data Science")
                    .courseCode("DS101")
                    .build();

            when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(1L))
                    .thenReturn(List.of(testCourse, course2));

            // Act
            List<Course> result = courseService.getAllCourses(1L);

            // Assert
            assertEquals(2, result.size());
            assertEquals("Computer Science", result.get(0).getName());
            assertEquals("Data Science", result.get(1).getName());
        }

        @Test
        @DisplayName("Should return empty list when no courses exist")
        void getAllCourses_empty_returnsEmptyList() {
            // Arrange
            when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(1L))
                    .thenReturn(new ArrayList<>());

            // Act
            List<Course> result = courseService.getAllCourses(1L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should exclude soft-deleted courses")
        void getAllCourses_excludesSoftDeleted() {
            // Arrange
            when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(1L))
                    .thenReturn(List.of(testCourse));

            // Act
            List<Course> result = courseService.getAllCourses(1L);

            // Assert
            assertEquals(1, result.size());
            verify(courseRepository).findByInstitution_IdAndDeletedAtIsNull(1L);
        }
    }

    @Nested
    @DisplayName("getCourseById - Happy Path Tests")
    class GetCourseByIdHappyPath {

        @Test
        @DisplayName("Should retrieve course by ID successfully")
        void getCourseById_success() {
            // Arrange
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            Course result = courseService.getCourseById(1L, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Computer Science", result.getName());
            assertEquals("CS101", result.getCourseCode());
        }

        @Test
        @DisplayName("Should retrieve course with all fields populated")
        void getCourseById_allFieldsPopulated() {
            // Arrange
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            Course result = courseService.getCourseById(1L, 1L);

            // Assert
            assertEquals("Computer Science", result.getName());
            assertEquals("CS101", result.getCourseCode());
            assertEquals("4 Years", result.getDuration());
            assertEquals(1, result.getSemester());
            assertEquals("https://s3.amazonaws.com/syllabus.pdf", result.getSyllabusUrl());
        }
    }

    @Nested
    @DisplayName("getCourseById - Unhappy Path Tests")
    class GetCourseByIdUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when course not found")
        void getCourseById_notFound_throwsException() {
            // Arrange
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.getCourseById(999L, 1L)
            );
        }

        @Test
        @DisplayName("Should not return soft-deleted courses")
        void getCourseById_softDeleted_throwsException() {
            // Arrange
            testCourse.setDeletedAt(LocalDateTime.now());
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.getCourseById(1L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("updateCourse - Happy Path Tests")
    class UpdateCourseHappyPath {

        @Test
        @DisplayName("Should successfully update course with all fields")
        void updateCourse_success() {
            // Arrange
            CreateCourseRequestDTO updateDTO = new CreateCourseRequestDTO(
                    1L, "Updated CS", "CS102", "3 Years", 2, testSyllabus
            );

            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(s3Service.uploadSyllabus(testSyllabus))
                    .thenReturn("https://s3.amazonaws.com/updated-syllabus.pdf");

            // Act
            courseService.updateCourse(1L, updateDTO, 1L);

            // Assert
            assertEquals("Updated CS", testCourse.getName());
            assertEquals("3 Years", testCourse.getDuration());
            assertEquals(2, testCourse.getSemester());
            assertEquals("https://s3.amazonaws.com/updated-syllabus.pdf", testCourse.getSyllabusUrl());

            verify(courseRepository).save(testCourse);
        }

        @Test
        @DisplayName("Should handle partial update - name only")
        void updateCourse_partialUpdate_nameOnly() {
            // Arrange
            CreateCourseRequestDTO partialDTO = new CreateCourseRequestDTO(
                    1L, "Updated Name", null, null, null, null
            );

            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            courseService.updateCourse(1L, partialDTO, 1L);

            // Assert
            assertEquals("Updated Name", testCourse.getName());
            assertEquals("4 Years", testCourse.getDuration()); // unchanged
            assertEquals(1, testCourse.getSemester()); // unchanged
        }

        @Test
        @DisplayName("Should update syllabus without updating other fields")
        void updateCourse_updateSyllabusOnly() {
            // Arrange
            CreateCourseRequestDTO updateDTO = new CreateCourseRequestDTO(
                    1L, null, null, null, null, testSyllabus
            );

            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(s3Service.uploadSyllabus(testSyllabus))
                    .thenReturn("https://s3.amazonaws.com/new-syllabus.pdf");

            // Act
            courseService.updateCourse(1L, updateDTO, 1L);

            // Assert
            assertEquals("Computer Science", testCourse.getName()); // unchanged
            assertEquals("https://s3.amazonaws.com/new-syllabus.pdf", testCourse.getSyllabusUrl());

            verify(s3Service).uploadSyllabus(testSyllabus);
        }

        @Test
        @DisplayName("Should not upload syllabus when empty file provided")
        void updateCourse_emptySyllabus_noUpload() {
            // Arrange
            MockMultipartFile emptySyllabus = new MockMultipartFile(
                    "syllabus", "test.pdf", "application/pdf", new byte[0]
            );
            CreateCourseRequestDTO updateDTO = new CreateCourseRequestDTO(
                    1L, "Updated", null, null, null, emptySyllabus
            );

            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            courseService.updateCourse(1L, updateDTO, 1L);

            // Assert
            verify(s3Service, never()).uploadSyllabus(any());
        }
    }

    @Nested
    @DisplayName("updateCourse - Unhappy Path Tests")
    class UpdateCourseUnhappyPath {

        @Test
        @DisplayName("Should throw exception when course not found during update")
        void updateCourse_notFound_throwsException() {
            // Arrange
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(999L, 1L))
                    .thenReturn(Optional.empty());

            CreateCourseRequestDTO updateDTO = new CreateCourseRequestDTO(
                    1L, "Updated", null, null, null, null
            );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.updateCourse(999L, updateDTO, 1L)
            );

            verify(courseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteCourse - Happy Path Tests")
    class DeleteCourseHappyPath {

        @Test
        @DisplayName("Should successfully soft delete course")
        void deleteCourse_success() {
            // Arrange
            assertNull(testCourse.getDeletedAt());
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            courseService.deleteCourse(1L, 1L);

            // Assert
            assertNotNull(testCourse.getDeletedAt());
            verify(courseRepository).save(testCourse);
        }

        @Test
        @DisplayName("Should set deletedAt to current timestamp")
        void deleteCourse_setsDeletedAtTimestamp() {
            // Arrange
            LocalDateTime before = LocalDateTime.now();
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            // Act
            courseService.deleteCourse(1L, 1L);

            // Assert
            LocalDateTime after = LocalDateTime.now();
            assertNotNull(testCourse.getDeletedAt());
            assertTrue(testCourse.getDeletedAt().isAfter(before.minusSeconds(1)));
            assertTrue(testCourse.getDeletedAt().isBefore(after.plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("deleteCourse - Unhappy Path Tests")
    class DeleteCourseUnhappyPath {

        @Test
        @DisplayName("Should throw exception when course not found during delete")
        void deleteCourse_notFound_throwsException() {
            // Arrange
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.deleteCourse(999L, 1L)
            );

            verify(courseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not delete already soft-deleted courses")
        void deleteCourse_alreadyDeleted_throwsException() {
            // Arrange
            testCourse.setDeletedAt(LocalDateTime.now());
            when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> courseService.deleteCourse(1L, 1L)
            );
        }
    }
}

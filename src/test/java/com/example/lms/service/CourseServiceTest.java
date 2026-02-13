package com.example.lms.service;

import com.example.lms.dto.CourseResponseDTO;
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
                "content".getBytes()
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

    /* =======================================================
                        CREATE COURSE
       ======================================================= */

    @Nested
    class CreateCourseTests {

        @Test
        void createCourse_success() {

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(false);
            when(s3Service.uploadSyllabus(testSyllabus))
                    .thenReturn("uploaded-url");

            courseService.createCourse(createDTO, 1L);

            ArgumentCaptor<Course> captor =
                    ArgumentCaptor.forClass(Course.class);

            verify(courseRepository).save(captor.capture());

            Course saved = captor.getValue();

            assertEquals("Computer Science", saved.getName());
            assertEquals("CS101", saved.getCourseCode());
            assertEquals("uploaded-url", saved.getSyllabusUrl());
            assertEquals(testInstitution, saved.getInstitution());
        }

        @Test
        void createCourse_withoutSyllabus() {

            CreateCourseRequestDTO dto =
                    new CreateCourseRequestDTO(
                            1L, "CS", "CS101", "4 Years", 1, null);

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(false);

            courseService.createCourse(dto, 1L);

            verify(s3Service, never()).uploadSyllabus(any());
            verify(courseRepository).save(any());
        }

        @Test
        void createCourse_institutionNotFound() {

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> courseService.createCourse(createDTO, 1L));

            verify(courseRepository, never()).save(any());
        }

        @Test
        void createCourse_duplicateCode() {

            when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                    .thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> courseService.createCourse(createDTO, 1L));

            verify(courseRepository, never()).save(any());
        }
    }

    /* =======================================================
                        GET ALL COURSES
       ======================================================= */

    @Nested
    @DisplayName("getAllCourses - Happy Path Tests")
    class GetAllCoursesHappyPath {

        @Test
        @DisplayName("Should retrieve all courses for institution")
        void getAllCourses_success() {

            Course course2 = Course.builder()
                    .id(2L)
                    .name("Data Science")
                    .courseCode("DS101")
                    .duration("3 Years")
                    .semester(2)
                    .institution(testInstitution)
                    .build();

            when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(1L))
                    .thenReturn(List.of(testCourse, course2));

            List<CourseResponseDTO> result =
                    courseService.getAllCourses(1L);

            assertEquals(2, result.size());
            assertEquals("Computer Science", result.get(0).name());
            assertEquals("Data Science", result.get(1).name());
        }

        @Test
        @DisplayName("Should return empty list when no courses exist")
        void getAllCourses_empty_returnsEmptyList() {

            when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(1L))
                    .thenReturn(new ArrayList<>());

            List<CourseResponseDTO> result =
                    courseService.getAllCourses(1L);

            assertTrue(result.isEmpty());
        }
    }


    /* =======================================================
                        GET COURSE BY ID
       ======================================================= */

    @Nested
    class GetCourseByIdTests {

        @Test
        void getCourseById_success() {

            when(courseRepository
                    .findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            Course result =
                    courseService.getCourseById(1L, 1L);

            assertEquals("CS101", result.getCourseCode());
        }

        @Test
        void getCourseById_notFound() {

            when(courseRepository
                    .findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> courseService.getCourseById(1L, 1L));
        }
    }

    /* =======================================================
                        UPDATE COURSE
       ======================================================= */

    @Nested
    class UpdateCourseTests {

        @Test
        void updateCourse_success() {

            when(courseRepository
                    .findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));
            when(s3Service.uploadSyllabus(testSyllabus))
                    .thenReturn("updated-url");

            courseService.updateCourse(1L, createDTO, 1L);

            assertEquals("updated-url",
                    testCourse.getSyllabusUrl());

            verify(courseRepository).save(testCourse);
        }

        @Test
        void updateCourse_notFound() {

            when(courseRepository
                    .findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> courseService.updateCourse(1L, createDTO, 1L));
        }
    }

    /* =======================================================
                        DELETE COURSE
       ======================================================= */

    @Nested
    class DeleteCourseTests {

        @Test
        void deleteCourse_success() {

            when(courseRepository
                    .findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.of(testCourse));

            courseService.deleteCourse(1L, 1L);

            assertNotNull(testCourse.getDeletedAt());
            verify(courseRepository).save(testCourse);
        }

        @Test
        void deleteCourse_notFound() {

            when(courseRepository
                    .findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> courseService.deleteCourse(1L, 1L));
        }
    }
}

package com.example.lms.service;

import com.example.lms.dto.CreateCourseRequestDTO;
import com.example.lms.entity.Course;
import com.example.lms.entity.Institution;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.InstitutionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private CourseService courseService;

    /* ================= CREATE ================= */

    @Test
    void createCourse_success() {

        Long institutionId = 1L;

        Institution institution = Institution.builder()
                .id(institutionId)
                .build();

        MockMultipartFile syllabus =
                new MockMultipartFile(
                        "syllabus",
                        "test.pdf",
                        "application/pdf",
                        "dummy".getBytes()
                );

        CreateCourseRequestDTO dto = new CreateCourseRequestDTO(
                institutionId,
                "Computer Science",
                "CS101",
                "4 Years",
                1,
                syllabus
        );

        when(institutionRepository.findByIdAndDeletedAtIsNull(institutionId))
                .thenReturn(Optional.of(institution));

        when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", institutionId))
                .thenReturn(false);

        when(s3Service.uploadSyllabus(any()))
                .thenReturn("s3-url");

        courseService.createCourse(dto, institutionId);

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_fail_institution_not_found() {

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        CreateCourseRequestDTO dto = new CreateCourseRequestDTO(
                1L,
                "CS",
                "CS101",
                "4 Years",
                1,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> courseService.createCourse(dto, 1L)
        );
    }

    @Test
    void createCourse_fail_duplicate_course_code() {

        Institution institution = Institution.builder().id(1L).build();

        when(institutionRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(institution));

        when(courseRepository.existsByCourseCodeAndInstitution_Id("CS101", 1L))
                .thenReturn(true);

        CreateCourseRequestDTO dto = new CreateCourseRequestDTO(
                1L,
                "CS",
                "CS101",
                "4 Years",
                1,
                null
        );

        assertThrows(
                IllegalStateException.class,
                () -> courseService.createCourse(dto, 1L)
        );
    }

    /* ================= GET ALL ================= */

    @Test
    void getAllCourses_success() {

        Long institutionId = 1L;

        Course c1 = Course.builder().name("CS").build();
        Course c2 = Course.builder().name("ME").build();

        when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(institutionId))
                .thenReturn(List.of(c1, c2));

        List<Course> result = courseService.getAllCourses(institutionId);

        assertEquals(2, result.size());
        verify(courseRepository).findByInstitution_IdAndDeletedAtIsNull(institutionId);
    }

    /* ================= GET BY ID ================= */

    @Test
    void getCourseById_success() {

        Course course = Course.builder().id(10L).build();

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(10L, 1L))
                .thenReturn(Optional.of(course));

        Course result = courseService.getCourseById(10L, 1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getCourseById_fail_not_found() {

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> courseService.getCourseById(1L, 1L)
        );
    }

    /* ================= UPDATE ================= */

    @Test
    void updateCourse_success() {

        Course course = Course.builder().id(1L).build();

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                .thenReturn(Optional.of(course));

        when(s3Service.uploadSyllabus(any()))
                .thenReturn("updated-s3-url");

        MockMultipartFile syllabus =
                new MockMultipartFile(
                        "syllabus",
                        "new.pdf",
                        "application/pdf",
                        "data".getBytes()
                );

        CreateCourseRequestDTO dto = new CreateCourseRequestDTO(
                1L,
                "Updated Course",
                "CS101",
                "3 Years",
                2,
                syllabus
        );

        courseService.updateCourse(1L, dto, 1L);

        verify(courseRepository).save(course);
        assertEquals("Updated Course", course.getName());
        assertEquals(2, course.getSemester());
    }

    /* ================= DELETE ================= */

    @Test
    void deleteCourse_success() {

        Course course = Course.builder().id(1L).build();

        when(courseRepository.findByIdAndInstitution_IdAndDeletedAtIsNull(1L, 1L))
                .thenReturn(Optional.of(course));

        courseService.deleteCourse(1L, 1L);

        assertNotNull(course.getDeletedAt());
        verify(courseRepository).save(course);
    }
}

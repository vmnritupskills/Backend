package com.example.lms.service;

import com.example.lms.dto.CmBootstrapResponseDTO;
import com.example.lms.dto.CourseSummaryDTO;
import com.example.lms.entity.ContentManager;
import com.example.lms.entity.Course;
import com.example.lms.entity.InstitutionCourseManager;
import com.example.lms.repository.ContentManagerRepository;
import com.example.lms.repository.InstitutionCourseManagerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentManagerBootstrapService Test Suite")
public class ContentManagerBootstrapServiceTest {

    @Mock
    private ContentManagerRepository cmRepo;

    @Mock
    private InstitutionCourseManagerRepository icmRepo;

    @InjectMocks
    private ContentManagerBootstrapService service;

    private ContentManager contentManager;
    private Course course1;
    private Course course2;
    private Course course3;
    private InstitutionCourseManager icm1;
    private InstitutionCourseManager icm2;

    @BeforeEach
    void setUp() {
        // Initialize content manager
        contentManager = ContentManager.builder()
                .name("John Doe")
                .build();
        contentManager.setId(1L);

        // Initialize courses
        course1 = new Course();
        course1.setId(1L);
        course1.setName("Java Basics");
        course1.setCourseCode("JAVA101");

        course2 = new Course();
        course2.setId(2L);
        course2.setName("Advanced Java");
        course2.setCourseCode("JAVA201");

        course3 = new Course();
        course3.setId(3L);
        course3.setName("Spring Boot");
        course3.setCourseCode("SPRING101");

        // Initialize institution course managers
        icm1 = new InstitutionCourseManager();
        icm1.setId(1L);
        icm1.setContentManager(contentManager);
        icm1.setCourse(course1);

        icm2 = new InstitutionCourseManager();
        icm2.setId(2L);
        icm2.setContentManager(contentManager);
        icm2.setCourse(course2);
    }

    // ===== HAPPY PATH TESTS =====

    @Test
    @DisplayName("Test 1: Should bootstrap CM with single course successfully")
    void testBootstrap_SingleCourse_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.cmId()).isEqualTo(1L);
        assertThat(result.cmName()).isEqualTo("John Doe");
        assertThat(result.courses()).hasSize(1);
        assertThat(result.courses().get(0).courseName()).isEqualTo("Java Basics");
        verify(cmRepo, times(1)).findByUser_Id(1L);
        verify(icmRepo, times(1)).findByContentManager_Id(1L);
    }

    @Test
    @DisplayName("Test 2: Should bootstrap CM with multiple courses successfully")
    void testBootstrap_MultipleCourses_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1, icm2));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.cmId()).isEqualTo(1L);
        assertThat(result.cmName()).isEqualTo("John Doe");
        assertThat(result.courses()).hasSize(2);
        assertThat(result.courses().get(0).courseName()).isEqualTo("Java Basics");
        assertThat(result.courses().get(1).courseName()).isEqualTo("Advanced Java");
        verify(cmRepo, times(1)).findByUser_Id(1L);
        verify(icmRepo, times(1)).findByContentManager_Id(1L);
    }

    @Test
    @DisplayName("Test 3: Should bootstrap CM with empty course list successfully")
    void testBootstrap_EmptyCourseList_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(List.of());

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.cmId()).isEqualTo(1L);
        assertThat(result.cmName()).isEqualTo("John Doe");
        assertThat(result.courses()).isEmpty();
        verify(cmRepo, times(1)).findByUser_Id(1L);
        verify(icmRepo, times(1)).findByContentManager_Id(1L);
    }

    @Test
    @DisplayName("Test 4: Should return correct course IDs in bootstrap response")
    void testBootstrap_VerifyCourseIds_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1, icm2));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result.courses().get(0).courseId()).isEqualTo(1L);
        assertThat(result.courses().get(1).courseId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Test 5: Should return correct course codes in bootstrap response")
    void testBootstrap_VerifyCourseCodes_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1, icm2));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result.courses().get(0).courseCode()).isEqualTo("JAVA101");
        assertThat(result.courses().get(1).courseCode()).isEqualTo("JAVA201");
    }

    @Test
    @DisplayName("Test 6: Should maintain order of courses in bootstrap response")
    void testBootstrap_CourseOrder_Success() {
        // Arrange
        List<InstitutionCourseManager> icmList = Arrays.asList(icm1, icm2);
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(icmList);

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result.courses()).extracting("courseCode")
                .containsExactly("JAVA101", "JAVA201");
    }

    @Test
    @DisplayName("Test 7: Should handle CM with many courses successfully")
    void testBootstrap_ManyCoursesSuccess() {
        // Arrange
        InstitutionCourseManager icm3 = new InstitutionCourseManager();
        icm3.setId(3L);
        icm3.setContentManager(contentManager);
        icm3.setCourse(course3);

        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1, icm2, icm3));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result.courses()).hasSize(3);
        verify(icmRepo, times(1)).findByContentManager_Id(1L);
    }

    @Test
    @DisplayName("Test 8: Should return CM name correctly in bootstrap")
    void testBootstrap_VerifyCmName_Success() {
        // Arrange
        ContentManager janeSmith = ContentManager.builder()
                .name("Jane Smith")
                .build();
        janeSmith.setId(1L);
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(janeSmith));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result.cmName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Test 9: Should handle special characters in course names")
    void testBootstrap_SpecialCharactersInCourseName_Success() {
        // Arrange
        course1.setName("Java & Advanced Programming (CORE)");
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result.courses().get(0).courseName()).isEqualTo("Java & Advanced Programming (CORE)");
    }

    @Test
    @DisplayName("Test 10: Should call repository methods exactly once")
    void testBootstrap_RepositoryCallCount_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1));

        // Act
        service.bootstrap(1L);

        // Assert
        verify(cmRepo, times(1)).findByUser_Id(1L);
        verify(icmRepo, times(1)).findByContentManager_Id(1L);
    }

    // ===== UNHAPPY PATH TESTS =====

    @Test
    @DisplayName("Test 11: Should throw exception when CM not found")
    void testBootstrap_CmNotFound_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CM not found");

        verify(cmRepo, times(1)).findByUser_Id(999L);
        verify(icmRepo, never()).findByContentManager_Id(any());
    }

    @Test
    @DisplayName("Test 12: Should throw exception when user ID is null")
    void testBootstrap_NullUserId_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CM not found");

        verify(icmRepo, never()).findByContentManager_Id(any());
    }

    @Test
    @DisplayName("Test 13: Should throw exception when user ID is negative")
    void testBootstrap_NegativeUserId_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CM not found");
    }

    @Test
    @DisplayName("Test 14: Should throw exception when user ID is zero")
    void testBootstrap_ZeroUserId_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(0L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CM not found");
    }

    @Test
    @DisplayName("Test 15: Should throw exception when CM repository throws exception")
    void testBootstrap_RepositoryException_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(1L))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");

        verify(icmRepo, never()).findByContentManager_Id(any());
    }

    @Test
    @DisplayName("Test 16: Should handle ICM repository exception gracefully")
    void testBootstrap_IcmRepositoryException_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L))
                .thenThrow(new RuntimeException("ICM repository error"));

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ICM repository error");
    }

    @Test
    @DisplayName("Test 17: Should handle very large user ID")
    void testBootstrap_LargeUserId_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(Long.MAX_VALUE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(Long.MAX_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CM not found");
    }

    @Test
    @DisplayName("Test 18: Should verify exception message is correct when CM not found")
    void testBootstrap_ExceptionMessage_Failure() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.bootstrap(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CM not found");
    }

    @Test
    @DisplayName("Test 19: Should handle multiple consecutive bootstrap calls")
    void testBootstrap_MultipleConsecutiveCalls_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1, icm2));

        // Act
        CmBootstrapResponseDTO result1 = service.bootstrap(1L);
        CmBootstrapResponseDTO result2 = service.bootstrap(1L);

        // Assert
        assertThat(result1.courses()).hasSize(2);
        assertThat(result2.courses()).hasSize(2);
        verify(cmRepo, times(2)).findByUser_Id(1L);
        verify(icmRepo, times(2)).findByContentManager_Id(1L);
    }

    @Test
    @DisplayName("Test 20: Should verify response type and structure")
    void testBootstrap_ResponseStructure_Success() {
        // Arrange
        when(cmRepo.findByUser_Id(1L)).thenReturn(Optional.of(contentManager));
        when(icmRepo.findByContentManager_Id(1L)).thenReturn(Arrays.asList(icm1));

        // Act
        CmBootstrapResponseDTO result = service.bootstrap(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(CmBootstrapResponseDTO.class);
        assertThat(result.cmId()).isNotNull();
        assertThat(result.cmName()).isNotNull();
        assertThat(result.courses()).isNotNull();
    }
}

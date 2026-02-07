package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentManagerService Tests")
class ContentManagerServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private ContentManagerRepository contentManagerRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstitutionCourseManagerRepository institutionCourseManagerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private ContentManagerService contentManagerService;

    private Institution testInstitution;
    private User testUser;
    private ContentManager testContentManager;
    private Role testRole;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        testInstitution = Institution.builder().id(1L).build();
        testRole = Role.builder().name("CONTENT_MANAGER").build();
        testUser = User.builder()
                .id(10L)
                .email("john@cms.com")
                .name("John")
                .password("encoded")
                .role(testRole)
                .isActive(true)
                .build();
        testContentManager = ContentManager.builder()
                .id(1L)
                .name("John")
                .email("john@cms.com")
                .department("CSE")
                .institution(testInstitution)
                .user(testUser)
                .build();
        testCourse = Course.builder().id(10L).name("Java").build();
    }

    @Nested
    @DisplayName("createContentManager - Happy Path Tests")
    class CreateContentManagerHappyPath {

        @Test
        @DisplayName("Should successfully create a new content manager")
        void createContentManager_success() {
            // Arrange
            Long institutionId = 1L;
            CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                    institutionId, "John", "john@cms.com", "password", "CSE"
            );

            when(institutionRepository.findById(institutionId))
                    .thenReturn(Optional.of(testInstitution));
            when(roleRepository.findByName("CONTENT_MANAGER"))
                    .thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode("password")).thenReturn("encoded");

            // Act
            contentManagerService.createContentManager(dto, institutionId);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertEquals("john@cms.com", savedUser.getEmail());
            assertEquals("John", savedUser.getName());
            assertEquals("encoded", savedUser.getPassword());
            assertTrue(savedUser.getIsActive());

            verify(contentManagerRepository).save(any(ContentManager.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void createContentManager_encodesPassword() {
            // Arrange
            CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                    1L, "John", "john@cms.com", "rawPassword", "CSE"
            );

            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(roleRepository.findByName("CONTENT_MANAGER"))
                    .thenReturn(Optional.of(testRole));
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

            // Act
            contentManagerService.createContentManager(dto, 1L);

            // Assert
            verify(passwordEncoder).encode("rawPassword");
        }
    }

    @Nested
    @DisplayName("createContentManager - Unhappy Path Tests")
    class CreateContentManagerUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when institution not found")
        void createContentManager_institutionNotFound_throwsException() {
            // Arrange
            CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                    999L, "John", "john@cms.com", "password", "CSE"
            );

            when(institutionRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.createContentManager(dto, 999L)
            );

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when CONTENT_MANAGER role not found")
        void createContentManager_roleNotFound_throwsException() {
            // Arrange
            CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                    1L, "John", "john@cms.com", "password", "CSE"
            );

            when(institutionRepository.findById(1L))
                    .thenReturn(Optional.of(testInstitution));
            when(roleRepository.findByName("CONTENT_MANAGER"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    RuntimeException.class,
                    () -> contentManagerService.createContentManager(dto, 1L)
            );
        }
    }

    @Nested
    @DisplayName("getContentManager - Happy Path Tests")
    class GetContentManagerHappyPath {

        @Test
        @DisplayName("Should retrieve content manager successfully")
        void getContentManager_success() {
            // Arrange
            when(contentManagerRepository.findByIdAndInstitutionId(1L, 1L))
                    .thenReturn(Optional.of(testContentManager));

            // Act
            ContentManagerDetailResponseDTO result =
                    contentManagerService.getContentManager(1L, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("John", result.name());
            assertEquals("john@cms.com", result.email());
            assertEquals("CSE", result.department());
            assertEquals(1L, result.institutionId());
            assertEquals(10L, result.userId());
            assertTrue(result.isActive());
        }
    }

    @Nested
    @DisplayName("getContentManager - Unhappy Path Tests")
    class GetContentManagerUnhappyPath {

        @Test
        @DisplayName("Should throw IllegalArgumentException when not found")
        void getContentManager_notFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findByIdAndInstitutionId(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.getContentManager(999L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("getAllContentManagers - Happy Path Tests")
    class GetAllContentManagersHappyPath {

        @Test
        @DisplayName("Should retrieve all content managers for institution")
        void getAllContentManagers_success() {
            // Arrange
            ContentManager cm2 = ContentManager.builder()
                    .id(2L)
                    .name("Jane")
                    .email("jane@cms.com")
                    .department("ECE")
                    .user(User.builder().id(11L).isActive(true).build())
                    .build();

            when(contentManagerRepository.findByInstitutionId(1L))
                    .thenReturn(List.of(testContentManager, cm2));

            // Act
            List<ContentManagerResponseDTO> result =
                    contentManagerService.getAllContentManagers(1L);

            // Assert
            assertEquals(2, result.size());
            assertEquals("John", result.get(0).name());
            assertEquals("Jane", result.get(1).name());
        }

        @Test
        @DisplayName("Should return empty list when no content managers exist")
        void getAllContentManagers_empty_returnsEmptyList() {
            // Arrange
            when(contentManagerRepository.findByInstitutionId(1L))
                    .thenReturn(new ArrayList<>());

            // Act
            List<ContentManagerResponseDTO> result =
                    contentManagerService.getAllContentManagers(1L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateContentManager - Happy Path Tests")
    class UpdateContentManagerHappyPath {

        @Test
        @DisplayName("Should successfully update content manager")
        void updateContentManager_success() {
            // Arrange
            UpdateContentManagerRequestDTO dto =
                    new UpdateContentManagerRequestDTO("Updated Name", "ECE", false);

            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));

            // Act
            contentManagerService.updateContentManager(1L, dto);

            // Assert
            assertEquals("Updated Name", testContentManager.getName());
            assertEquals("ECE", testContentManager.getDepartment());
            assertFalse(testContentManager.getUser().getIsActive());

            verify(userRepository).save(testUser);
            verify(contentManagerRepository).save(testContentManager);
        }

        @Test
        @DisplayName("Should toggle active status")
        void updateContentManager_togglesActiveStatus() {
            // Arrange
            assertTrue(testContentManager.getUser().getIsActive());
            UpdateContentManagerRequestDTO dto =
                    new UpdateContentManagerRequestDTO("John", "CSE", false);

            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));

            // Act
            contentManagerService.updateContentManager(1L, dto);

            // Assert
            assertFalse(testContentManager.getUser().getIsActive());
        }
    }

    @Nested
    @DisplayName("updateContentManager - Unhappy Path Tests")
    class UpdateContentManagerUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found")
        void updateContentManager_notFound_throwsException() {
            // Arrange
            UpdateContentManagerRequestDTO dto =
                    new UpdateContentManagerRequestDTO("Updated", "ECE", true);

            when(contentManagerRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.updateContentManager(999L, dto)
            );
        }
    }

    @Nested
    @DisplayName("deleteContentManager - Happy Path Tests")
    class DeleteContentManagerHappyPath {

        @Test
        @DisplayName("Should successfully delete content manager")
        void deleteContentManager_success() {
            // Arrange
            when(contentManagerRepository.findByIdAndInstitutionId(1L, 1L))
                    .thenReturn(Optional.of(testContentManager));

            // Act
            contentManagerService.deleteContentManager(1L, 1L);

            // Assert
            verify(institutionCourseManagerRepository).deleteByContentManager_Id(1L);
            verify(userRepository).delete(testUser);
            verify(contentManagerRepository).delete(testContentManager);
        }
    }

    @Nested
    @DisplayName("deleteContentManager - Unhappy Path Tests")
    class DeleteContentManagerUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found")
        void deleteContentManager_notFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findByIdAndInstitutionId(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.deleteContentManager(999L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("assignCourseToContentManager - Happy Path Tests")
    class AssignCourseHappyPath {

        @Test
        @DisplayName("Should successfully assign course to content manager")
        void assignCourse_success() {
            // Arrange
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(courseRepository.findById(10L))
                    .thenReturn(Optional.of(testCourse));
            when(institutionCourseManagerRepository.existsByCourse_IdAndContentManager_Id(10L, 1L))
                    .thenReturn(false);

            // Act
            contentManagerService.assignCourseToContentManager(10L, 1L);

            // Assert
            ArgumentCaptor<InstitutionCourseManager> captor =
                    ArgumentCaptor.forClass(InstitutionCourseManager.class);
            verify(institutionCourseManagerRepository).save(captor.capture());

            InstitutionCourseManager saved = captor.getValue();
            assertEquals(testContentManager, saved.getContentManager());
            assertEquals(testCourse, saved.getCourse());
            assertEquals(testInstitution, saved.getInstitution());
        }
    }

    @Nested
    @DisplayName("assignCourseToContentManager - Unhappy Path Tests")
    class AssignCourseUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found")
        void assignCourse_cmNotFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.assignCourseToContentManager(10L, 999L)
            );
        }

        @Test
        @DisplayName("Should throw exception when course not found")
        void assignCourse_courseNotFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(courseRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.assignCourseToContentManager(999L, 1L)
            );
        }

        @Test
        @DisplayName("Should throw exception when course already assigned")
        void assignCourse_alreadyAssigned_throwsException() {
            // Arrange
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(courseRepository.findById(10L))
                    .thenReturn(Optional.of(testCourse));
            when(institutionCourseManagerRepository.existsByCourse_IdAndContentManager_Id(10L, 1L))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(
                    IllegalStateException.class,
                    () -> contentManagerService.assignCourseToContentManager(10L, 1L)
            );

            verify(institutionCourseManagerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("bulkAssignCoursesToContentManager - Happy Path Tests")
    class BulkAssignCoursesHappyPath {

        @Test
        @DisplayName("Should successfully assign multiple courses")
        void bulkAssignCourses_success() {
            // Arrange
            List<Long> courseIds = List.of(10L, 11L, 12L);
            Course course2 = Course.builder().id(11L).name("Python").build();
            Course course3 = Course.builder().id(12L).name("C++").build();

            when(contentManagerRepository.findByIdAndInstitutionId(1L, 1L))
                    .thenReturn(Optional.of(testContentManager));
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(courseRepository.findById(10L)).thenReturn(Optional.of(testCourse));
            when(courseRepository.findById(11L)).thenReturn(Optional.of(course2));
            when(courseRepository.findById(12L)).thenReturn(Optional.of(course3));
            when(institutionCourseManagerRepository.existsByCourse_IdAndContentManager_Id(anyLong(), eq(1L)))
                    .thenReturn(false);

            // Act
            contentManagerService.bulkAssignCoursesToContentManager(courseIds, 1L, 1L);

            // Assert
            verify(institutionCourseManagerRepository, times(3)).save(any());
        }
    }

    @Nested
    @DisplayName("bulkAssignCoursesToContentManager - Unhappy Path Tests")
    class BulkAssignCoursesUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found in bulk assign")
        void bulkAssignCourses_cmNotFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findByIdAndInstitutionId(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.bulkAssignCoursesToContentManager(List.of(10L), 999L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("unassignCourseFromContentManager - Happy Path Tests")
    class UnassignCourseHappyPath {

        @Test
        @DisplayName("Should successfully unassign course")
        void unassignCourse_success() {
            // Arrange
            InstitutionCourseManager mapping = InstitutionCourseManager.builder()
                    .id(1L)
                    .build();

            when(institutionCourseManagerRepository.findByCourse_IdAndContentManager_Id(10L, 1L))
                    .thenReturn(Optional.of(mapping));

            // Act
            contentManagerService.unassignCourseFromContentManager(10L, 1L);

            // Assert
            verify(institutionCourseManagerRepository).delete(mapping);
        }
    }

    @Nested
    @DisplayName("unassignCourseFromContentManager - Unhappy Path Tests")
    class UnassignCourseUnhappyPath {

        @Test
        @DisplayName("Should throw exception when assignment not found")
        void unassignCourse_notFound_throwsException() {
            // Arrange
            when(institutionCourseManagerRepository.findByCourse_IdAndContentManager_Id(10L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.unassignCourseFromContentManager(10L, 1L)
            );
        }
    }

    @Nested
    @DisplayName("getCoursesAssignedToContentManager - Happy Path Tests")
    class GetAssignedCoursesHappyPath {

        @Test
        @DisplayName("Should retrieve all assigned courses")
        void getAssignedCourses_success() {
            // Arrange
            InstitutionCourseManager m1 = InstitutionCourseManager.builder()
                    .course(testCourse)
                    .build();
            InstitutionCourseManager m2 = InstitutionCourseManager.builder()
                    .course(Course.builder().id(11L).name("Python").build())
                    .build();

            when(institutionCourseManagerRepository.findByContentManager_Id(1L))
                    .thenReturn(List.of(m1, m2));

            // Act
            List<Course> result =
                    contentManagerService.getCoursesAssignedToContentManager(1L);

            // Assert
            assertEquals(2, result.size());
            assertEquals("Java", result.get(0).getName());
            assertEquals("Python", result.get(1).getName());
        }

        @Test
        @DisplayName("Should return empty list when no courses assigned")
        void getAssignedCourses_empty_returnsEmptyList() {
            // Arrange
            when(institutionCourseManagerRepository.findByContentManager_Id(1L))
                    .thenReturn(new ArrayList<>());

            // Act
            List<Course> result =
                    contentManagerService.getCoursesAssignedToContentManager(1L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getContentManagerStats - Happy Path Tests")
    class GetStatsHappyPath {

        @Test
        @DisplayName("Should retrieve content manager statistics")
        void getStats_success() {
            // Arrange
            when(contentManagerRepository.count()).thenReturn(10L);
            when(contentManagerRepository.countActiveContentManagers()).thenReturn(8L);

            // Act
            ContentManagerStatsResponseDTO result =
                    contentManagerService.getContentManagerStats();

            // Assert
            assertNotNull(result);
            assertEquals(10L, result.total());
            assertEquals(8L, result.active());
        }
    }

    @Nested
    @DisplayName("getStudentCountUnderContentManager - Happy Path Tests")
    class GetStudentCountHappyPath {

        @Test
        @DisplayName("Should retrieve student count under content manager")
        void getStudentCount_success() {
            // Arrange
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(enrollmentRepository.countStudentsUnderContentManager(1L))
                    .thenReturn(25L);

            // Act
            ContentManagerStudentStatsResponseDTO result =
                    contentManagerService.getStudentCountUnderContentManager(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.cmId());
            assertEquals(25L, result.totalStudents());
        }
    }

    @Nested
    @DisplayName("getStudentCountUnderContentManager - Unhappy Path Tests")
    class GetStudentCountUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found")
        void getStudentCount_cmNotFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.getStudentCountUnderContentManager(999L)
            );
        }
    }

    @Nested
    @DisplayName("getStudentsUnderContentManager - Happy Path Tests")
    class GetStudentsListHappyPath {

        @Test
        @DisplayName("Should retrieve list of students under content manager")
        void getStudents_success() {
            // Arrange
            List<EnrolledStudentResponseDTO> students = List.of(
                    new EnrolledStudentResponseDTO(1L, "Student1", "stu1@test.com"),
                    new EnrolledStudentResponseDTO(2L, "Student2", "stu2@test.com")
            );

            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(enrollmentRepository.findStudentsUnderContentManager(1L))
                    .thenReturn(students);

            // Act
            List<EnrolledStudentResponseDTO> result =
                    contentManagerService.getStudentsUnderContentManager(1L);

            // Assert
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("getStudentsUnderContentManager - Unhappy Path Tests")
    class GetStudentsListUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found")
        void getStudents_cmNotFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.getStudentsUnderContentManager(999L)
            );
        }
    }

    @Nested
    @DisplayName("updatePassword - Happy Path Tests")
    class UpdatePasswordHappyPath {

        @Test
        @DisplayName("Should successfully update password")
        void updatePassword_success() {
            // Arrange
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(passwordEncoder.encode("newPassword"))
                    .thenReturn("encodedNewPassword");

            // Act
            contentManagerService.updatePassword(1L, "newPassword");

            // Assert
            assertEquals("encodedNewPassword", testContentManager.getUser().getPassword());
            verify(passwordEncoder).encode("newPassword");
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("updatePassword - Unhappy Path Tests")
    class UpdatePasswordUnhappyPath {

        @Test
        @DisplayName("Should throw exception when content manager not found")
        void updatePassword_cmNotFound_throwsException() {
            // Arrange
            when(contentManagerRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> contentManagerService.updatePassword(999L, "newPassword")
            );
        }
    }

    @Nested
    @DisplayName("updateAssignedCourses - Happy Path Tests")
    class UpdateAssignedCoursesHappyPath {

        @Test
        @DisplayName("Should replace assigned courses successfully")
        void updateAssignedCourses_success() {
            // Arrange
            List<Long> courseIds = List.of(10L, 11L);
            Course course2 = Course.builder().id(11L).name("Python").build();

            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));
            when(courseRepository.findById(10L))
                    .thenReturn(Optional.of(testCourse));
            when(courseRepository.findById(11L))
                    .thenReturn(Optional.of(course2));
            when(institutionCourseManagerRepository.existsByCourse_IdAndContentManager_Id(anyLong(), eq(1L)))
                    .thenReturn(false);

            // Act
            contentManagerService.updateAssignedCourses(courseIds, 1L);

            // Assert
            verify(institutionCourseManagerRepository).deleteByContentManager_Id(1L);
            verify(institutionCourseManagerRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("Should handle empty course list")
        void updateAssignedCourses_emptyList_success() {
            // Arrange
            when(contentManagerRepository.findById(1L))
                    .thenReturn(Optional.of(testContentManager));

            // Act
            contentManagerService.updateAssignedCourses(new ArrayList<>(), 1L);

            // Assert
            verify(institutionCourseManagerRepository).deleteByContentManager_Id(1L);
            verify(institutionCourseManagerRepository, never()).save(any());
        }
    }
}

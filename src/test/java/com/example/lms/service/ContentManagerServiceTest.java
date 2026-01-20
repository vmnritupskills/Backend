package com.example.lms.service;

import com.example.lms.dto.CreateContentManagerRequestDTO;
import com.example.lms.dto.UpdateContentManagerRequestDTO;
import com.example.lms.dto.ContentManagerResponseDTO;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private ContentManagerService contentManagerService;

    /* ================= CREATE ================= */

    @Test
    void createContentManager_success() {

        Long institutionId = 1L;

        Institution institution = Institution.builder()
                .id(institutionId)
                .build();

        Role role = Role.builder()
                .name("CONTENT_MANAGER")
                .build();

        CreateContentManagerRequestDTO dto =
                new CreateContentManagerRequestDTO(
                        institutionId,
                        "John",
                        "john@cms.com",
                        "password",
                        "CSE"
                );

        when(institutionRepository.findById(institutionId))
                .thenReturn(Optional.of(institution));

        when(roleRepository.findByName("CONTENT_MANAGER"))
                .thenReturn(Optional.of(role));

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded");

        contentManagerService.createContentManager(dto, institutionId);

        verify(userRepository).save(any(User.class));
        verify(contentManagerRepository).save(any(ContentManager.class));
    }

    @Test
    void createContentManager_fail_institution_not_found() {

        when(institutionRepository.findById(1L))
                .thenReturn(Optional.empty());

        CreateContentManagerRequestDTO dto =
                new CreateContentManagerRequestDTO(
                        1L, "John", "john@cms.com", "pass", "CSE"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> contentManagerService.createContentManager(dto, 1L)
        );
    }

    /* ================= GET ================= */

    @Test
    void getContentManager_success() {

        ContentManager cm = ContentManager.builder()
                .id(1L)
                .build();

        when(contentManagerRepository.findByIdAndInstitutionId(1L, 1L))
                .thenReturn(Optional.of(cm));

        ContentManager result =
                contentManagerService.getContentManager(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getContentManager_not_found() {

        when(contentManagerRepository.findByIdAndInstitutionId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> contentManagerService.getContentManager(1L, 1L)
        );
    }

    @Test
    void getAllContentManagers_success() {

        User user = User.builder().id(10L).isActive(true).build();

        ContentManager cm = ContentManager.builder()
                .id(1L)
                .name("John")
                .email("john@cms.com")
                .department("CSE")
                .user(user)
                .build();

        when(contentManagerRepository.findByInstitutionId(1L))
                .thenReturn(List.of(cm));

        List<ContentManagerResponseDTO> result =
                contentManagerService.getAllContentManagers(1L);

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).name());
    }

    /* ================= UPDATE ================= */

    @Test
    void updateContentManager_success() {

        User user = User.builder().isActive(true).build();

        ContentManager cm = ContentManager.builder()
                .id(1L)
                .user(user)
                .build();

        UpdateContentManagerRequestDTO dto =
                new UpdateContentManagerRequestDTO(
                        "Updated",
                        "ECE",
                        false
                );

        when(contentManagerRepository.findById(1L))
                .thenReturn(Optional.of(cm));

        contentManagerService.updateContentManager(1L, dto);

        assertEquals("Updated", cm.getName());
        assertEquals("ECE", cm.getDepartment());
        assertFalse(cm.getUser().getIsActive());

        verify(userRepository).save(user);
        verify(contentManagerRepository).save(cm);
    }

    /* ================= DELETE ================= */

    @Test
    void deleteContentManager_success() {

        User user = User.builder().build();

        ContentManager cm = ContentManager.builder()
                .id(1L)
                .user(user)
                .build();

        when(contentManagerRepository.findByIdAndInstitutionId(1L, 1L))
                .thenReturn(Optional.of(cm));

        contentManagerService.deleteContentManager(1L, 1L);

        verify(institutionCourseManagerRepository)
                .deleteByContentManagerId(1L);

        verify(userRepository).delete(user);
        verify(contentManagerRepository).delete(cm);
    }

    /* ================= ASSIGN COURSE ================= */

    @Test
    void assignCourse_success() {

        ContentManager cm = ContentManager.builder().id(1L).build();
        Course course = Course.builder().id(10L).build();

        when(contentManagerRepository.findById(1L))
                .thenReturn(Optional.of(cm));

        when(courseRepository.findById(10L))
                .thenReturn(Optional.of(course));

        when(institutionCourseManagerRepository
                .existsByCourseIdAndContentManagerId(10L, 1L))
                .thenReturn(false);

        contentManagerService.assignCourseToContentManager(10L, 1L);

        verify(institutionCourseManagerRepository)
                .save(any(InstitutionCourseManager.class));
    }

    @Test
    void assignCourse_fail_already_assigned() {

        ContentManager cm = ContentManager.builder()
                .id(1L)
                .build();

        Course course = Course.builder()
                .id(10L)
                .build();

        when(contentManagerRepository.findById(1L))
                .thenReturn(Optional.of(cm));

        when(courseRepository.findById(10L))
                .thenReturn(Optional.of(course));

        when(institutionCourseManagerRepository
                .existsByCourseIdAndContentManagerId(10L, 1L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> contentManagerService.assignCourseToContentManager(10L, 1L)
        );

        verify(institutionCourseManagerRepository, never())
                .save(any());
    }


    /* ================= UNASSIGN ================= */

    @Test
    void unassignCourse_success() {

        InstitutionCourseManager mapping =
                InstitutionCourseManager.builder().build();

        when(institutionCourseManagerRepository
                .findByCourseIdAndContentManagerId(10L, 1L))
                .thenReturn(Optional.of(mapping));

        contentManagerService.unassignCourseFromContentManager(10L, 1L);

        verify(institutionCourseManagerRepository).delete(mapping);
    }

    /* ================= GET ASSIGNED COURSES ================= */

    @Test
    void getCoursesAssignedToContentManager_success() {

        Course c1 = Course.builder().name("CS").build();
        Course c2 = Course.builder().name("ME").build();

        InstitutionCourseManager m1 =
                InstitutionCourseManager.builder().course(c1).build();

        InstitutionCourseManager m2 =
                InstitutionCourseManager.builder().course(c2).build();

        when(institutionCourseManagerRepository.findByContentManagerId(1L))
                .thenReturn(List.of(m1, m2));

        List<Course> result =
                contentManagerService.getCoursesAssignedToContentManager(1L);

        assertEquals(2, result.size());
    }

    /* ================= UPDATE ASSIGNED COURSES ================= */

    @Test
    void updateAssignedCourses_success() {

        doNothing().when(institutionCourseManagerRepository)
                .deleteByContentManagerId(1L);

        ContentManager cm = ContentManager.builder().id(1L).build();
        Course course = Course.builder().id(10L).build();

        when(contentManagerRepository.findById(1L))
                .thenReturn(Optional.of(cm));

        when(courseRepository.findById(10L))
                .thenReturn(Optional.of(course));

        when(institutionCourseManagerRepository
                .existsByCourseIdAndContentManagerId(10L, 1L))
                .thenReturn(false);

        contentManagerService.updateAssignedCourses(
                List.of(10L),
                1L
        );

        verify(institutionCourseManagerRepository)
                .deleteByContentManagerId(1L);
    }
}

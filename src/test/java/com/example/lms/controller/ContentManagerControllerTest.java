package com.example.lms.controller;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.dto.*;
import com.example.lms.entity.Course;
import com.example.lms.exception.ForbiddenException;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.service.ContentManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ContentManagerController.class, SecurityConfig.class})
@Import({GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("ContentManagerController Tests")
class ContentManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ContentManagerService contentManagerService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // ==================== CREATE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create content manager with valid data should return 200")
    void testCreateContentManager_Success() throws Exception {
        CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                1L, "John Doe", "john@example.com", "Pass@123", "Computer Science"
        );

        doNothing().when(contentManagerService).createContentManager(any(CreateContentManagerRequestDTO.class), eq(1L));

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Content Manager created successfully"));

        verify(contentManagerService).createContentManager(any(CreateContentManagerRequestDTO.class), eq(1L));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Create content manager with CONTENT_MANAGER role should return 403")
    void testCreateContentManager_Forbidden() throws Exception {
        CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                1L, "John Doe", "john@example.com", "Pass@123", "Computer Science"
        );

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(contentManagerService, never()).createContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create content manager with missing name should return 400")
    void testCreateContentManager_MissingName() throws Exception {
        String jsonPayload = """
                {
                    "institutionId": 1,
                    "email": "john@example.com",
                    "password": "Pass@123",
                    "department": "CS"
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).createContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create content manager with missing email should return 400")
    void testCreateContentManager_MissingEmail() throws Exception {
        String jsonPayload = """
                {
                    "institutionId": 1,
                    "name": "John Doe",
                    "password": "Pass@123",
                    "department": "CS"
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).createContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create content manager with missing password should return 400")
    void testCreateContentManager_MissingPassword() throws Exception {
        String jsonPayload = """
                {
                    "institutionId": 1,
                    "name": "John Doe",
                    "email": "john@example.com",
                    "department": "CS"
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).createContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create content manager with missing department should return 400")
    void testCreateContentManager_MissingDepartment() throws Exception {
        String jsonPayload = """
                {
                    "institutionId": 1,
                    "name": "John Doe",
                    "email": "john@example.com",
                    "password": "Pass@123"
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).createContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Create content manager with null institutionId should return 400")
    void testCreateContentManager_NullInstitutionId() throws Exception {
        String jsonPayload = """
                {
                    "institutionId": null,
                    "name": "John Doe",
                    "email": "john@example.com",
                    "password": "Pass@123",
                    "department": "CS"
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).createContentManager(any(), any());
    }

    // ==================== GET ALL TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get all content managers should return 200 with list")
    void testGetAllContentManagers_Success() throws Exception {
        ContentManagerResponseDTO response = new ContentManagerResponseDTO(
                1L, "John Doe", "john@example.com", "IT", 10L, true
        );

        when(contentManagerService.getAllContentManagers(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/institution/content-managers")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[0].department").value("IT"))
                .andExpect(jsonPath("$[0].isActive").value(true));

        verify(contentManagerService).getAllContentManagers(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get all content managers with empty list should return 200")
    void testGetAllContentManagers_EmptyList() throws Exception {
        when(contentManagerService.getAllContentManagers(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/institution/content-managers")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(contentManagerService).getAllContentManagers(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Get all content managers with CONTENT_MANAGER role should return 403")
    void testGetAllContentManagers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/institution/content-managers")
                        .param("institutionId", "1"))
                .andExpect(status().isForbidden());

        verify(contentManagerService, never()).getAllContentManagers(any());
    }

    // ==================== GET BY ID TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get content manager by ID should return 200")
    void testGetContentManager_Success() throws Exception {
        ContentManagerDetailResponseDTO response = new ContentManagerDetailResponseDTO(
                1L, "John Doe", "john@example.com", "CS", 1L, 1L, true
        );

        when(contentManagerService.getContentManager(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/institution/content-managers/1")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.department").value("CS"));

        verify(contentManagerService).getContentManager(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get non-existent content manager should return 404")
    void testGetContentManager_NotFound() throws Exception {
        when(contentManagerService.getContentManager(999L, 1L))
                .thenThrow(new ResourceNotFoundException("Content Manager not found"));

        mockMvc.perform(get("/api/institution/content-managers/999")
                        .param("institutionId", "1"))
                .andExpect(status().isNotFound());

        verify(contentManagerService).getContentManager(999L, 1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Get content manager by ID as CONTENT_MANAGER should return 200")
    void testGetContentManager_AsContentManager_Success() throws Exception {
        ContentManagerDetailResponseDTO response = new ContentManagerDetailResponseDTO(
                1L, "John Doe", "john@example.com", "CS", 1L, 1L, true
        );

        when(contentManagerService.getContentManager(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/institution/content-managers/1")
                        .param("institutionId", "1"))
                .andExpect(status().isOk());

        verify(contentManagerService).getContentManager(1L, 1L);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update content manager with valid data should return 200")
    void testUpdateContentManager_Success() throws Exception {
        UpdateContentManagerRequestDTO dto = new UpdateContentManagerRequestDTO(
                "John Updated", "Electronics", true
        );

        doNothing().when(contentManagerService).updateContentManager(eq(1L), any(UpdateContentManagerRequestDTO.class));

        mockMvc.perform(put("/api/institution/content-managers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Content Manager updated successfully"));

        verify(contentManagerService).updateContentManager(eq(1L), any(UpdateContentManagerRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Update content manager as CONTENT_MANAGER should return 200 if updating self")
    void testUpdateContentManager_AsContentManager_Success() throws Exception {
        UpdateContentManagerRequestDTO dto = new UpdateContentManagerRequestDTO(
                "John Updated", "Electronics", true
        );

        doNothing().when(contentManagerService).updateContentManager(eq(1L), any(UpdateContentManagerRequestDTO.class));

        mockMvc.perform(put("/api/institution/content-managers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(contentManagerService).updateContentManager(eq(1L), any(UpdateContentManagerRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update content manager with missing name should return 400")
    void testUpdateContentManager_MissingName() throws Exception {
        String jsonPayload = """
                {
                    "department": "CS",
                    "isActive": true
                }
                """;

        mockMvc.perform(put("/api/institution/content-managers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).updateContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update content manager with null isActive should return 400")
    void testUpdateContentManager_NullIsActive() throws Exception {
        String jsonPayload = """
                {
                    "name": "John",
                    "department": "CS",
                    "isActive": null
                }
                """;

        mockMvc.perform(put("/api/institution/content-managers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).updateContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update non-existent content manager should return 404")
    void testUpdateContentManager_NotFound() throws Exception {
        UpdateContentManagerRequestDTO dto = new UpdateContentManagerRequestDTO(
                "John Updated", "Electronics", true
        );

        doThrow(new ResourceNotFoundException("Content Manager not found"))
                .when(contentManagerService).updateContentManager(eq(999L), any());

        mockMvc.perform(put("/api/institution/content-managers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(contentManagerService).updateContentManager(eq(999L), any());
    }

    // ==================== DELETE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete content manager should return 200")
    void testDeleteContentManager_Success() throws Exception {
        doNothing().when(contentManagerService).deleteContentManager(1L, 1L);

        mockMvc.perform(delete("/api/institution/content-managers/1")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Content Manager deleted successfully"));

        verify(contentManagerService).deleteContentManager(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Delete content manager with CONTENT_MANAGER role should return 403")
    void testDeleteContentManager_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/institution/content-managers/1")
                        .param("institutionId", "1"))
                .andExpect(status().isForbidden());

        verify(contentManagerService, never()).deleteContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Delete non-existent content manager should return 404")
    void testDeleteContentManager_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Content Manager not found"))
                .when(contentManagerService).deleteContentManager(999L, 1L);

        mockMvc.perform(delete("/api/institution/content-managers/999")
                        .param("institutionId", "1"))
                .andExpect(status().isNotFound());

        verify(contentManagerService).deleteContentManager(999L, 1L);
    }

    // ==================== ASSIGN COURSE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Assign single course to content manager should return 200")
    void testAssignCourse_Success() throws Exception {
        AssignCoursesToContentManagerDTO dto = new AssignCoursesToContentManagerDTO(1L);

        doNothing().when(contentManagerService).assignCourseToContentManager(1L, 1L);

        mockMvc.perform(post("/api/institution/content-managers/1/courses/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Course assigned successfully"));

        verify(contentManagerService).assignCourseToContentManager(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Assign course with missing courseId should return 400")
    void testAssignCourse_MissingCourseId() throws Exception {
        String jsonPayload = "{}";

        mockMvc.perform(post("/api/institution/content-managers/1/courses/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).assignCourseToContentManager(any(), any());
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Assign course with CONTENT_MANAGER role should return 403")
    void testAssignCourse_Forbidden() throws Exception {
        AssignCoursesToContentManagerDTO dto = new AssignCoursesToContentManagerDTO(1L);

        mockMvc.perform(post("/api/institution/content-managers/1/courses/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(contentManagerService, never()).assignCourseToContentManager(any(), any());
    }

    // ==================== BULK ASSIGN COURSES TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Bulk assign courses to content manager should return 200")
    void testBulkAssignCourses_Success() throws Exception {
        BulkAssignCoursesToContentManagerDTO dto = new BulkAssignCoursesToContentManagerDTO(
                1L, List.of(1L, 2L, 3L)
        );

        doNothing().when(contentManagerService).bulkAssignCoursesToContentManager(
                List.of(1L, 2L, 3L), 1L, 1L
        );

        mockMvc.perform(post("/api/institution/content-managers/1/courses/assign/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Courses assigned successfully"));

        verify(contentManagerService).bulkAssignCoursesToContentManager(
                List.of(1L, 2L, 3L), 1L, 1L
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Bulk assign with empty course list should return 400")
    void testBulkAssignCourses_EmptyList() throws Exception {
        String jsonPayload = """
                {
                    "institutionId": 1,
                    "courseIds": []
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers/1/courses/assign/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).bulkAssignCoursesToContentManager(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Bulk assign with missing institutionId should return 400")
    void testBulkAssignCourses_MissingInstitutionId() throws Exception {
        String jsonPayload = """
                {
                    "courseIds": [1, 2, 3]
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers/1/courses/assign/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).bulkAssignCoursesToContentManager(any(), any(), any());
    }

    // ==================== UPDATE PASSWORD TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update password should return 200")
    void testUpdatePassword_Success() throws Exception {
        UpdatePasswordRequestDTO dto = new UpdatePasswordRequestDTO("NewPass@123");

        doNothing().when(contentManagerService).updatePassword(1L, "NewPass@123");

        mockMvc.perform(put("/api/institution/content-managers/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));

        verify(contentManagerService).updatePassword(1L, "NewPass@123");
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Update password as CONTENT_MANAGER should return 200")
    void testUpdatePassword_AsContentManager_Success() throws Exception {
        UpdatePasswordRequestDTO dto = new UpdatePasswordRequestDTO("NewPass@123");

        doNothing().when(contentManagerService).updatePassword(1L, "NewPass@123");

        mockMvc.perform(put("/api/institution/content-managers/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(contentManagerService).updatePassword(1L, "NewPass@123");
    }

    // ==================== UNASSIGN COURSE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Unassign course from content manager should return 200")
    void testUnassignCourse_Success() throws Exception {
        doNothing().when(contentManagerService).unassignCourseFromContentManager(1L, 1L);

        mockMvc.perform(delete("/api/institution/content-managers/1/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Course unassigned successfully"));

        verify(contentManagerService).unassignCourseFromContentManager(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Unassign course with CONTENT_MANAGER role should return 403")
    void testUnassignCourse_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/institution/content-managers/1/courses/1"))
                .andExpect(status().isForbidden());

        verify(contentManagerService, never()).unassignCourseFromContentManager(any(), any());
    }

    // ==================== GET ASSIGNED COURSES TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get assigned courses should return 200 with course list")
    void testGetAssignedCourses_Success() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");

        when(contentManagerService.getCoursesAssignedToContentManager(1L))
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/institution/content-managers/1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Basics"));

        verify(contentManagerService).getCoursesAssignedToContentManager(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Get assigned courses as CONTENT_MANAGER should return 200")
    void testGetAssignedCourses_AsContentManager_Success() throws Exception {
        when(contentManagerService.getCoursesAssignedToContentManager(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/institution/content-managers/1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(contentManagerService).getCoursesAssignedToContentManager(1L);
    }

    // ==================== UPDATE ASSIGNED COURSES TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update assigned courses should return 200")
    void testUpdateAssignedCourses_Success() throws Exception {
        UpdateAssignedCoursesDTO dto = new UpdateAssignedCoursesDTO(List.of(1L, 2L));

        doNothing().when(contentManagerService).updateAssignedCourses(List.of(1L, 2L), 1L);

        mockMvc.perform(put("/api/institution/content-managers/1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Assigned courses updated successfully"));

        verify(contentManagerService).updateAssignedCourses(List.of(1L, 2L), 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Update assigned courses with empty list should return 400")
    void testUpdateAssignedCourses_EmptyList() throws Exception {
        String jsonPayload = """
                {
                    "courseIds": []
                }
                """;

        mockMvc.perform(put("/api/institution/content-managers/1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());

        verify(contentManagerService, never()).updateAssignedCourses(any(), any());
    }

    // ==================== STATS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get content manager stats should return 200")
    void testGetContentManagerStats_Success() throws Exception {
        ContentManagerStatsResponseDTO stats = new ContentManagerStatsResponseDTO(
                10L, 7L
        );

        when(contentManagerService.getContentManagerStats()).thenReturn(stats);

        mockMvc.perform(get("/api/institution/content-managers/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContentManagers").value(10))
                .andExpect(jsonPath("$.activeContentManagers").value(7));

        verify(contentManagerService).getContentManagerStats();
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Get stats with CONTENT_MANAGER role should return 403")
    void testGetContentManagerStats_Forbidden() throws Exception {
        mockMvc.perform(get("/api/institution/content-managers/admin/stats"))
                .andExpect(status().isForbidden());

        verify(contentManagerService, never()).getContentManagerStats();
    }

    // ==================== STUDENT COUNT TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get student count under content manager should return 200")
    void testGetStudentCountUnderContentManager_Success() throws Exception {
        ContentManagerStudentStatsResponseDTO stats = new ContentManagerStudentStatsResponseDTO(1L, 25L);

        when(contentManagerService.getStudentCountUnderContentManager(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/institution/content-managers/1/students/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(25));

        verify(contentManagerService).getStudentCountUnderContentManager(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Get student count as CONTENT_MANAGER should return 200")
    void testGetStudentCountUnderContentManager_AsContentManager_Success() throws Exception {
        ContentManagerStudentStatsResponseDTO stats = new ContentManagerStudentStatsResponseDTO(1L, 10L);

        when(contentManagerService.getStudentCountUnderContentManager(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/institution/content-managers/1/students/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(10));

        verify(contentManagerService).getStudentCountUnderContentManager(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get student count for non-existent manager should return 404")
    void testGetStudentCountUnderContentManager_NotFound() throws Exception {
        when(contentManagerService.getStudentCountUnderContentManager(999L))
                .thenThrow(new ResourceNotFoundException("Content Manager not found"));

        mockMvc.perform(get("/api/institution/content-managers/999/students/count"))
                .andExpect(status().isNotFound());

        verify(contentManagerService).getStudentCountUnderContentManager(999L);
    }

    // ==================== GET STUDENTS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get students under content manager should return 200 with student list")
    void testGetStudentsUnderContentManager_Success() throws Exception {
        EnrolledStudentResponseDTO student = new EnrolledStudentResponseDTO(
                1L, "Alice", "alice@example.com", "Java Basics"
        );

        when(contentManagerService.getStudentsUnderContentManager(1L))
                .thenReturn(List.of(student));

        mockMvc.perform(get("/api/institution/content-managers/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].courseTitle").value("Java Basics"));

        verify(contentManagerService).getStudentsUnderContentManager(1L);
    }

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    @DisplayName("Get students as CONTENT_MANAGER should return 200")
    void testGetStudentsUnderContentManager_AsContentManager_Success() throws Exception {
        when(contentManagerService.getStudentsUnderContentManager(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/institution/content-managers/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(contentManagerService).getStudentsUnderContentManager(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get students for non-existent manager should return 404")
    void testGetStudentsUnderContentManager_NotFound() throws Exception {
        when(contentManagerService.getStudentsUnderContentManager(999L))
                .thenThrow(new ResourceNotFoundException("Content Manager not found"));

        mockMvc.perform(get("/api/institution/content-managers/999/students"))
                .andExpect(status().isNotFound());

        verify(contentManagerService).getStudentsUnderContentManager(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Get students with empty list should return 200")
    void testGetStudentsUnderContentManager_EmptyList() throws Exception {
        when(contentManagerService.getStudentsUnderContentManager(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/institution/content-managers/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(contentManagerService).getStudentsUnderContentManager(1L);
    }
}
package com.example.lms;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.controller.ContentManagerController;
import com.example.lms.dto.*;
import com.example.lms.entity.ContentManager;
import com.example.lms.exception.GlobalExceptionHandler;
import com.example.lms.service.ContentManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ContentManagerController.class, SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class ContentManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ContentManagerService contentManagerService;

    @MockitoBean
    private JwtUtil jwtUtil;

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void createContentManager_success() throws Exception {
        // Updated to match your record: institutionId, name, email, password, department
        CreateContentManagerRequestDTO dto = new CreateContentManagerRequestDTO(
                1L, "John Doe", "john@example.com", "Pass@123", "Computer Science"
        );

        doNothing().when(contentManagerService).createContentManager(any(), eq(1L));

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Content Manager created successfully"));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void getAllContentManagers_success() throws Exception {
        // Matches your ContentManagerResponseDTO fields
        ContentManagerResponseDTO response = new ContentManagerResponseDTO(
                1L, "John Doe", "john@example.com", "IT", 10L, true
        );

        when(contentManagerService.getAllContentManagers(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/institution/content-managers")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].department").value("IT"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void updateContentManager_success() throws Exception {
        // Matches UpdateContentManagerRequestDTO: name, department, isActive
        UpdateContentManagerRequestDTO dto = new UpdateContentManagerRequestDTO(
                "John Updated", "Electronics", true
        );

        doNothing().when(contentManagerService).updateContentManager(eq(1L), any(UpdateContentManagerRequestDTO.class));

        mockMvc.perform(put("/api/institution/content-managers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Content Manager updated successfully"));
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void deleteContentManager_success() throws Exception {
        doNothing().when(contentManagerService).deleteContentManager(1L, 1L);

        mockMvc.perform(delete("/api/institution/content-managers/1")
                        .param("institutionId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Content Manager deleted successfully"));
    }

    /* ================= SECURITY & VALIDATION ================= */

    @Test
    @WithMockUser(roles = "CONTENT_MANAGER")
    void deleteContentManager_forbidden() throws Exception {
        mockMvc.perform(delete("/api/institution/content-managers/1")
                        .param("institutionId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void createContentManager_badRequest_missingDepartment() throws Exception {

        String invalidJson = """
                {
                    "institutionId": 1,
                    "name": "John",
                    "email": "john@example.com",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/institution/content-managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("department")));
    }
}
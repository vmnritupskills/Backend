package com.example.lms.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.lms.config.JwtUtil;
import com.example.lms.config.SecurityConfig;
import com.example.lms.dto.CreateInstitutionRequestDTO;
import com.example.lms.dto.InstitutionResponseDTO;
import com.example.lms.dto.UpdateInstitutionRequestDTO;
import com.example.lms.service.InstitutionService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({InstitutionController.class, SecurityConfig.class})
@ActiveProfiles("test")
class InstitutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private InstitutionService institutionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstitution_success() throws Exception {

        CreateInstitutionRequestDTO dto =
                new CreateInstitutionRequestDTO(
                        "NRIT",
                        "admin@nrit.edu",
                        "Password@123",
                        "Vijayawada",
                        "AISHE123"
                );

        doNothing().when(institutionService).createInstitution(dto);

        mockMvc.perform(
                        post("/api/admin/institutions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Institution created successfully"));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllInstitutions_success() throws Exception {

        InstitutionResponseDTO inst1 =
                new InstitutionResponseDTO(
                        1L,
                        "NRIT",
                        "Vijayawada",
                        "AISHE123",
                        10L,
                        true,
                        LocalDateTime.now()
                );

        InstitutionResponseDTO inst2 =
                new InstitutionResponseDTO(
                        2L,
                        "ABC College",
                        "Hyderabad",
                        "AISHE999",
                        20L,
                        true,
                        LocalDateTime.now()
                );

        when(institutionService.getAllInstitutions())
                .thenReturn(List.of(inst1, inst2));

        mockMvc.perform(get("/api/admin/institutions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("NRIT"))
                .andExpect(jsonPath("$[1].aisheCode").value("AISHE999"));
    }

    /* ================= GET BY ID ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInstitutionById_success() throws Exception {

        InstitutionResponseDTO response =
                new InstitutionResponseDTO(
                        1L,
                        "NRIT",
                        "Vijayawada",
                        "AISHE123",
                        10L,
                        true,
                        LocalDateTime.now()
                );

        when(institutionService.getInstitutionById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/admin/institutions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NRIT"))
                .andExpect(jsonPath("$.aisheCode").value("AISHE123"));
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateInstitution_success() throws Exception {

        UpdateInstitutionRequestDTO dto =
                new UpdateInstitutionRequestDTO(
                        "Updated NRIT",
                        "Updated Address",
                        "NEW-AISHE",
                        false
                );

        doNothing().when(institutionService).updateInstitution(1L, dto);

        mockMvc.perform(
                        put("/api/admin/institutions/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Institution updated successfully"));
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteInstitution_success() throws Exception {

        doNothing().when(institutionService).deleteInstitution(1L);

        mockMvc.perform(delete("/api/admin/institutions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Institution deleted successfully"));
    }

    /* ================= SECURITY ================= */

    @Test
    @WithMockUser(roles = "INSTITUTION")
    void createInstitution_forbidden_for_non_admin() throws Exception {

        CreateInstitutionRequestDTO dto =
                new CreateInstitutionRequestDTO(
                        "NRIT",
                        "admin@nrit.edu",
                        "Password@123",
                        "Vijayawada",
                        "AISHE123"
                );

        mockMvc.perform(
                        post("/api/admin/institutions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isForbidden());
    }
}

package com.example.lms.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.lms.dto.CreateInstitutionRequestDTO;
import com.example.lms.dto.UpdateInstitutionRequestDTO;
import com.example.lms.dto.InstitutionResponseDTO;
import com.example.lms.service.InstitutionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/admin/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    /* ================= CREATE ================= */
    @Operation(
            summary = "Create Institution",
            description = "Admin creates a new institution"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Institution created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error / Email already exists"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN allowed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> createInstitution(
            @RequestBody CreateInstitutionRequestDTO dto) {

        institutionService.createInstitution(dto);
        return ResponseEntity.ok("Institution created successfully");
    }

    /* ================= GET ALL ================= */
    @Operation(
            summary = "Get all Institutions",
            description = "Admin fetches all active institutions"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Institutions fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN allowed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<InstitutionResponseDTO>> getAllInstitutions() {

        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    /* ================= GET BY ID ================= */
    @Operation(
            summary = "Get Institution by ID",
            description = "Fetch institution details by institution ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Institution fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Institution not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN','INSTITUTION')")
    @GetMapping("/{id}")
    public ResponseEntity<InstitutionResponseDTO> getInstitutionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(institutionService.getInstitutionById(id));
    }

    /* ================= UPDATE ================= */
    @Operation(
            summary = "Update Institution",
            description = "Admin or Institution updates institution details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Institution updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Institution not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN','INSTITUTION')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateInstitution(
            @PathVariable Long id,
            @RequestBody UpdateInstitutionRequestDTO dto) {

        institutionService.updateInstitution(id, dto);
        return ResponseEntity.ok("Institution updated successfully");
    }

    /* ================= DELETE (SOFT) ================= */
    @Operation(
            summary = "Delete Institution",
            description = "Admin soft-deletes an institution"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Institution deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN allowed"),
            @ApiResponse(responseCode = "404", description = "Institution not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInstitution(
            @PathVariable Long id) {

        institutionService.deleteInstitution(id);
        return ResponseEntity.ok("Institution deleted successfully");
    }
}

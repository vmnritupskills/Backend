package com.example.lms.controller;

import com.example.lms.dto.CmBootstrapResponseDTO;
import com.example.lms.service.ContentManagerBootstrapService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cm")
@RequiredArgsConstructor
public class ContentManagerBootstrapController {

    private final ContentManagerBootstrapService service;

    @GetMapping("/bootstrap")
    public ResponseEntity<CmBootstrapResponseDTO> bootstrap(
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("User ID not found in request");
        }

        return ResponseEntity.ok(
                service.bootstrap(userId)
        );
    }
}

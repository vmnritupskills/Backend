package com.example.lms.dto;

public record LoginResponseDTO(
        String token,
        Long userId,
        String username,
        String email,
        String role
) {

}

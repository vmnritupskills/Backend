package com.example.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @Email(message = "Invalid email format")
		@NotBlank(message="Email should not be null")
        String email,
        @NotBlank(message="Password should not be null")
        String password
) {


}


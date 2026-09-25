package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 45)
    private String username;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank @Email
    private String email;

    private String gender;

    @NotBlank
    private String country;

    @NotBlank
    private String phoneno;
}

package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Email
    private String email;

    @NotBlank
    private String country;

    @NotBlank
    private String phoneno;
}

package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlacklistRequest {
    @NotBlank
    private String malicious;
}

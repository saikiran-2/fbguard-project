package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppDecisionRequest {
    @NotBlank
    private String decision; // "APPROVE" | "REJECT"
}

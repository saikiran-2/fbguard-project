package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank
    private String msgto;

    @NotBlank
    private String msg;
}

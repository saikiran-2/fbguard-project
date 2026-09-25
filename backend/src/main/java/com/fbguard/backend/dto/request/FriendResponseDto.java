package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendResponseDto {
    @NotBlank
    private String rfrom; // requester username

    @NotBlank
    private String status; // "Accept" | "Reject"
}

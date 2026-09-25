package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendRequestDto {
    @NotBlank
    private String rto; // target username
}

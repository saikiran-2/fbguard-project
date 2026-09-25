package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private Long uid;
        private String username;
        private String role;
        private String photoUrl;
    }
}

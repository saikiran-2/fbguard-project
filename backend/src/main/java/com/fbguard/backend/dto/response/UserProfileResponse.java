package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long uid;
    private String username;
    private String email;
    private String gender;
    private String country;
    private String phoneno;
    private String photoUrl;
    private String role;
}

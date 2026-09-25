package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendDto {
    private String username;
    private String photoUrl;
}

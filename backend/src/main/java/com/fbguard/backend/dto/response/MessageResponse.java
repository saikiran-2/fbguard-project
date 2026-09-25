package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long mid;
    private String msgfrom;
    private String msgto;
    private String msg;
    private LocalDateTime sentAt;
}

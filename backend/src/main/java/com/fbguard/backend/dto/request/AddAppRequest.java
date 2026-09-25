package com.fbguard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddAppRequest {
    @NotBlank
    private String appname;

    @NotBlank
    private String appid;

    @NotBlank
    private String appurl;
}

package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskBucketResponse {
    private String bucket;
    private long count;
}

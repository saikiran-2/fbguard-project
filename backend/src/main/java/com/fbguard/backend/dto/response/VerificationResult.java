package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Built via the Builder pattern by AppVerificationService (a Facade over several
 * VerificationStrategy implementations) and returned to the caller right after submission.
 */
@Data
@Builder
public class VerificationResult {
    private int riskScore;      // 0-100, higher = more suspicious
    private String riskLevel;   // LOW | MEDIUM | HIGH
    private String message;     // human readable summary
    private String signalsSummary;
}

package com.fbguard.backend.service.verification;

import lombok.Builder;
import lombok.Value;

/** Immutable result of one VerificationStrategy run. */
@Value
@Builder
public class RiskSignal {
    int score;       // 0 (safe) - 100 (definitely malicious)
    String detail;   // short human-readable explanation
}

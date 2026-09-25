package com.fbguard.backend.kafka.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Fired every time a user submits an app. Decouples the "an app was submitted"
 * fact from whatever needs to react to it (audit logging, admin notifications,
 * analytics) - new listeners can be added without touching AppController.
 */
public record AppSubmittedEvent(
        Long aid,
        String appname,
        String appurl,
        String submittedByUsername,
        int riskScore,
        String riskLevel,
        LocalDateTime occurredAt
) implements Serializable {
}

package com.fbguard.backend.kafka.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Fired every time an admin approves or rejects a pending app. */
public record AppDecisionEvent(
        Long aid,
        String appname,
        String decision,
        String submitterUsername,
        String decidedByUsername,
        LocalDateTime occurredAt
) implements Serializable {
}

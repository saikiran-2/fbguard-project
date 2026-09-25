package com.fbguard.backend.kafka;

import com.fbguard.backend.kafka.event.AppDecisionEvent;
import com.fbguard.backend.kafka.event.AppSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes domain events to Kafka. Callers (services/controllers) never know
 * or care who consumes these - that's the whole point of the event bus.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppEventProducer {

    public static final String SUBMISSIONS_TOPIC = "app-submissions";
    public static final String DECISIONS_TOPIC = "app-decisions";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSubmitted(AppSubmittedEvent event) {
        try {
            kafkaTemplate.send(SUBMISSIONS_TOPIC, event.aid().toString(), event);
        } catch (Exception e) {
            // Kafka being briefly unavailable should never break the user-facing submit flow.
            log.warn("Could not publish AppSubmittedEvent for app {}: {}", event.aid(), e.getMessage());
        }
    }

    public void publishDecision(AppDecisionEvent event) {
        try {
            kafkaTemplate.send(DECISIONS_TOPIC, event.aid().toString(), event);
        } catch (Exception e) {
            log.warn("Could not publish AppDecisionEvent for app {}: {}", event.aid(), e.getMessage());
        }
    }
}

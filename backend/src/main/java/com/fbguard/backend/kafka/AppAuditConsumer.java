package com.fbguard.backend.kafka;

import com.fbguard.backend.kafka.event.AppDecisionEvent;
import com.fbguard.backend.kafka.event.AppSubmittedEvent;
import com.fbguard.backend.repository.UserRepository;
import com.fbguard.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Observer: reacts to submission/decision events asynchronously, independent
 * of the request thread that created them. This consumer does two things -
 * both completely decoupled from AppController/AppService, which never know
 * this class exists:
 *   1. always writes an audit log line
 *   2. emails the admin when a submission comes in HIGH risk, and emails the
 *      submitter when an admin makes a manual decision
 * A third reaction (fraud analytics, Slack, etc.) would just be a third
 * @KafkaListener method here or a whole new @Component - no other code changes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppAuditConsumer {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @KafkaListener(topics = AppEventProducer.SUBMISSIONS_TOPIC, groupId = "fbguard-audit")
    public void onAppSubmitted(AppSubmittedEvent event) {
        log.info("[AUDIT] app '{}' (#{}) submitted by {} -> risk {} ({})",
                event.appname(), event.aid(), event.submittedByUsername(),
                event.riskScore(), event.riskLevel());

        if ("HIGH".equals(event.riskLevel())) {
            emailService.notifyAdminOfHighRiskSubmission(
                    event.appname(), event.appurl(), event.riskScore(), event.submittedByUsername());
        }
    }

    @KafkaListener(topics = AppEventProducer.DECISIONS_TOPIC, groupId = "fbguard-audit")
    public void onAppDecision(AppDecisionEvent event) {
        log.info("[AUDIT] app '{}' (#{}) {} by {}",
                event.appname(), event.aid(), event.decision(), event.decidedByUsername());

        userRepository.findByUsername(event.submitterUsername())
                .ifPresent(submitter -> emailService.notifySubmitterOfDecision(
                        submitter.getEmail(), event.appname(), event.decision()));
    }
}

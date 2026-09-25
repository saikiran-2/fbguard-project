package com.fbguard.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends notification emails via SMTP (works with Gmail's free SMTP relay,
 * or any free provider like Resend/Brevo). If mail isn't configured
 * (MAIL_USERNAME unset), this degrades to just logging - same pattern as
 * SafeBrowsingStrategy, so the app never breaks just because a feature's
 * credentials aren't set up yet.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.admin.email:admin@fbguard.local}")
    private String adminEmail;

    @Value("${app.notifications.enabled:false}")
    private boolean notificationsEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyAdminOfHighRiskSubmission(String appname, String appurl, int riskScore, String submittedBy) {
        String subject = "[FBGuard] High-risk app blocked: " + appname;
        String body = """
                An app submission was automatically rejected by the verification engine.

                App name: %s
                App URL: %s
                Risk score: %d/100
                Submitted by: %s

                No action needed - this was handled automatically. Log in to the admin
                panel if you'd like to review the individual signal breakdown.
                """.formatted(appname, appurl, riskScore, submittedBy);

        send(adminEmail, subject, body);
    }

    public void notifySubmitterOfDecision(String toEmail, String appname, String decision) {
        String subject = "[FBGuard] Your app '" + appname + "' was " + decision.toLowerCase();
        String body = "Hi,\n\nYour app submission '" + appname + "' has been " + decision.toLowerCase()
                + " by an admin after review.\n\n- FBGuard";
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (!notificationsEnabled || fromAddress == null || fromAddress.isBlank()) {
            log.info("[EMAIL disabled/not configured] Would have sent to {}: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // A flaky mail provider should never fail the request that triggered it.
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

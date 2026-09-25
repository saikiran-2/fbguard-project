package com.fbguard.backend.service.verification;

import org.springframework.stereotype.Component;

/**
 * Zero-cost heuristic scoring based purely on URL shape - no external calls,
 * no API keys. Mirrors the kind of static rules real trust & safety systems
 * use as a cheap first pass before spending API quota on deeper checks.
 */
@Component
public class UrlHeuristicStrategy implements VerificationStrategy {

    @Override
    public RiskSignal evaluate(String appUrl) {
        String host = UrlUtils.extractHost(appUrl);
        int score = 0;
        StringBuilder reasons = new StringBuilder();

        if (UrlUtils.looksLikeIp(appUrl)) {
            score += 30;
            reasons.append("uses a raw IP address; ");
        }
        if (UrlUtils.isShortener(appUrl)) {
            score += 25;
            reasons.append("uses a URL shortener; ");
        }
        if (UrlUtils.hasSuspiciousTld(host)) {
            score += 20;
            reasons.append("uses a commonly-abused free TLD; ");
        }
        if (!UrlUtils.isHttps(appUrl)) {
            score += 15;
            reasons.append("not served over HTTPS; ");
        }
        if (!UrlUtils.isOfficialFacebookHost(host)) {
            score += 10;
            reasons.append("not hosted on an official facebook.com domain; ");
        }
        if (host != null && host.chars().filter(c -> c == '-').count() >= 3) {
            score += 10;
            reasons.append("domain has an unusually high number of hyphens; ");
        }

        score = Math.min(score, 100);
        String detail = reasons.isEmpty() ? "No suspicious URL patterns found" : reasons.toString().trim();
        return RiskSignal.builder().score(score).detail(detail).build();
    }

    @Override
    public String getName() {
        return "URL Heuristics";
    }

    @Override
    public double getWeight() {
        return 0.25;
    }
}

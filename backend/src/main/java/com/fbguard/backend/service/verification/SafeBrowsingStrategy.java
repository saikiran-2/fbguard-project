package com.fbguard.backend.service.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Calls Google's free Safe Browsing Lookup API (10,000 requests/day free quota,
 * no billing account needed). If no API key is configured this strategy scores
 * 0 and says so, rather than failing the whole verification pipeline.
 */
@Component
@Slf4j
public class SafeBrowsingStrategy implements VerificationStrategy {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://safebrowsing.googleapis.com")
            .build();

    @Value("${verification.safe-browsing.api-key:}")
    private String apiKey;

    @Override
    public RiskSignal evaluate(String appUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            return RiskSignal.builder().score(0).detail("Safe Browsing not configured (no API key)").build();
        }

        try {
            Map<String, Object> body = Map.of(
                    "client", Map.of("clientId", "fbguard", "clientVersion", "1.0.0"),
                    "threatInfo", Map.of(
                            "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE"),
                            "platformTypes", List.of("ANY_PLATFORM"),
                            "threatEntryTypes", List.of("URL"),
                            "threatEntries", List.of(Map.of("url", appUrl))
                    )
            );

            Map<?, ?> response = webClient.post()
                    .uri("/v4/threatMatches:find?key={key}", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();

            boolean flagged = response != null && response.containsKey("matches");
            if (flagged) {
                return RiskSignal.builder().score(100).detail("Flagged by Google Safe Browsing").build();
            }
            return RiskSignal.builder().score(0).detail("Clean per Google Safe Browsing").build();
        } catch (Exception e) {
            log.warn("Safe Browsing check failed for {}: {}", appUrl, e.getMessage());
            return RiskSignal.builder().score(0).detail("Safe Browsing check unavailable").build();
        }
    }

    @Override
    public String getName() {
        return "Google Safe Browsing";
    }

    @Override
    public double getWeight() {
        return 0.25;
    }
}

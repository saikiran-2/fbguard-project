package com.fbguard.backend.service.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Domain-age check using the free, keyless RDAP protocol (the modern
 * successor to WHOIS; rdap.org auto-routes to the right registry).
 * Newly-registered domains are a classic scam/phishing signal.
 */
@Component
@Slf4j
public class DomainAgeStrategy implements VerificationStrategy {

    private final WebClient webClient = WebClient.builder().baseUrl("https://rdap.org").build();

    @Override
    public RiskSignal evaluate(String appUrl) {
        String host = UrlUtils.extractHost(appUrl);
        if (host == null) {
            return RiskSignal.builder().score(0).detail("Could not parse domain for age check").build();
        }
        String registrableDomain = toRegistrableDomain(host);

        try {
            Map<?, ?> response = webClient.get()
                    .uri("/domain/{domain}", registrableDomain)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();

            if (response == null) {
                return RiskSignal.builder().score(0).detail("Domain age unavailable").build();
            }

            List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
            if (events == null) {
                return RiskSignal.builder().score(0).detail("No registration event data").build();
            }

            String registrationDate = events.stream()
                    .filter(e -> "registration".equals(e.get("eventAction")))
                    .map(e -> (String) e.get("eventDate"))
                    .findFirst()
                    .orElse(null);

            if (registrationDate == null) {
                return RiskSignal.builder().score(0).detail("Registration date not found").build();
            }

            long ageDays = ChronoUnit.DAYS.between(Instant.parse(registrationDate), Instant.now());
            if (ageDays < 30) {
                return RiskSignal.builder().score(70).detail("Domain registered " + ageDays + " days ago").build();
            } else if (ageDays < 180) {
                return RiskSignal.builder().score(30).detail("Domain registered " + ageDays + " days ago").build();
            }
            return RiskSignal.builder().score(0).detail("Domain is " + ageDays + " days old").build();

        } catch (Exception e) {
            log.debug("RDAP lookup failed for {}: {}", registrableDomain, e.getMessage());
            return RiskSignal.builder().score(0).detail("Domain age lookup unavailable").build();
        }
    }

    /** Naive eTLD+1 extraction - good enough for common two-part TLDs like .com/.org/.net. */
    private String toRegistrableDomain(String host) {
        String[] parts = host.split("\\.");
        if (parts.length <= 2) return host;
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }

    @Override
    public String getName() {
        return "Domain Age (RDAP)";
    }

    @Override
    public double getWeight() {
        return 0.15;
    }
}

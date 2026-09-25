package com.fbguard.backend.service.verification;

import com.fbguard.backend.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fast-path check against the admin-curated blacklist table (equivalent to the
 * original project's `malicious` table lookup). Kept as one signal among several
 * instead of the sole gatekeeper.
 */
@Component
@RequiredArgsConstructor
public class BlacklistCheckStrategy implements VerificationStrategy {

    private final BlacklistRepository blacklistRepository;

    @Override
    public RiskSignal evaluate(String appUrl) {
        String host = UrlUtils.extractHost(appUrl);
        boolean blocked = blacklistRepository.findAll().stream()
                .anyMatch(entry -> appUrl.contains(entry.getMalicious())
                        || (host != null && host.contains(entry.getMalicious())));

        if (blocked) {
            return RiskSignal.builder()
                    .score(100)
                    .detail("URL matches an entry on the admin blacklist")
                    .build();
        }
        return RiskSignal.builder().score(0).detail("Not present on blacklist").build();
    }

    @Override
    public String getName() {
        return "Local Blacklist";
    }

    @Override
    public double getWeight() {
        return 0.35;
    }
}

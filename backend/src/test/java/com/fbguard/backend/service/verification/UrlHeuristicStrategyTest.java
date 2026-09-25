package com.fbguard.backend.service.verification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test - no Spring context, no mocks. UrlHeuristicStrategy has
 * zero dependencies, so we just call it and assert on the score. This is
 * the fastest, cheapest kind of test to write and should cover most of
 * your business logic.
 */
class UrlHeuristicStrategyTest {

    private final UrlHeuristicStrategy strategy = new UrlHeuristicStrategy();

    @Test
    void officialFacebookHttpsUrl_scoresLow() {
        RiskSignal signal = strategy.evaluate("https://apps.facebook.com/mygame");
        assertThat(signal.getScore()).isLessThanOrEqualTo(10);
    }

    @Test
    void rawIpAddress_addsRiskScore() {
        RiskSignal signal = strategy.evaluate("http://192.168.1.50/claim-prize");
        assertThat(signal.getScore()).isGreaterThanOrEqualTo(30);
        assertThat(signal.getDetail()).contains("IP address");
    }

    @Test
    void urlShortener_addsRiskScore() {
        RiskSignal signal = strategy.evaluate("https://bit.ly/free-coins-now");
        assertThat(signal.getDetail()).contains("URL shortener");
    }

    @Test
    void suspiciousTld_addsRiskScore() {
        RiskSignal signal = strategy.evaluate("http://totally-legit-app.tk/play");
        assertThat(signal.getDetail()).contains("free TLD");
    }

    @Test
    void combinedRedFlags_scoreCapsAt100() {
        // IP + shortener-like pattern + suspicious TLD + no https - should stack but never exceed 100
        RiskSignal signal = strategy.evaluate("http://192.168.1.1.tk/bit.ly-clone");
        assertThat(signal.getScore()).isLessThanOrEqualTo(100);
    }
}

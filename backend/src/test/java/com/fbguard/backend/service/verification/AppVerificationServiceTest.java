package com.fbguard.backend.service.verification;

import com.fbguard.backend.dto.response.VerificationResult;
import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the Facade in isolation by mocking every VerificationStrategy - we're
 * testing "does the weighted-average math work", not "does Google's API
 * respond correctly" (that would be a separate, slower integration test).
 */
@ExtendWith(MockitoExtension.class)
class AppVerificationServiceTest {

    @Mock
    private VerificationStrategy strategyA; // weight 0.6

    @Mock
    private VerificationStrategy strategyB; // weight 0.4

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    private AppVerificationService service;
    private AppSubmission submission;

    @BeforeEach
    void setUp() {
        service = new AppVerificationService(List.of(strategyA, strategyB), riskAssessmentRepository);

        submission = AppSubmission.builder()
                .aid(1L)
                .appname("Test App")
                .appurl("https://apps.facebook.com/testapp")
                .submittedBy(User.builder().username("alice").build())
                .build();
    }

    @Test
    void weightedAverage_combinesScoresCorrectly() {
        when(strategyA.getName()).thenReturn("Strategy A");
        when(strategyA.getWeight()).thenReturn(0.6);
        when(strategyA.evaluate(submission.getAppurl()))
                .thenReturn(RiskSignal.builder().score(100).detail("flagged").build());

        when(strategyB.getName()).thenReturn("Strategy B");
        when(strategyB.getWeight()).thenReturn(0.4);
        when(strategyB.evaluate(submission.getAppurl()))
                .thenReturn(RiskSignal.builder().score(0).detail("clean").build());

        VerificationResult result = service.verify(submission);

        // (100*0.6 + 0*0.4) / (0.6+0.4) = 60
        assertThat(result.getRiskScore()).isEqualTo(60);
        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    void allStrategiesClean_resultsInLowRisk() {
        when(strategyA.getName()).thenReturn("Strategy A");
        when(strategyA.getWeight()).thenReturn(0.6);
        when(strategyA.evaluate(submission.getAppurl()))
                .thenReturn(RiskSignal.builder().score(0).detail("clean").build());

        when(strategyB.getName()).thenReturn("Strategy B");
        when(strategyB.getWeight()).thenReturn(0.4);
        when(strategyB.evaluate(submission.getAppurl()))
                .thenReturn(RiskSignal.builder().score(0).detail("clean").build());

        VerificationResult result = service.verify(submission);

        assertThat(result.getRiskScore()).isZero();
        assertThat(result.getRiskLevel()).isEqualTo("LOW");
    }
}

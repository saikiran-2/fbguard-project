package com.fbguard.backend.service.verification;

import com.fbguard.backend.dto.response.VerificationResult;
import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.RiskAssessment;
import com.fbguard.backend.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Facade pattern: callers only ever talk to this one service. Internally it fans
 * out to every registered VerificationStrategy (Spring injects the full list -
 * adding a new check is just adding a new @Component, no code here changes),
 * combines their scores by weight, and persists an explainable audit trail.
 */
@Service
@RequiredArgsConstructor
public class AppVerificationService {

    private final List<VerificationStrategy> strategies;
    private final RiskAssessmentRepository riskAssessmentRepository;

    public VerificationResult verify(AppSubmission submission) {
        double weightedTotal = 0;
        double weightSum = 0;
        StringBuilder signals = new StringBuilder();

        for (VerificationStrategy strategy : strategies) {
            RiskSignal signal = strategy.evaluate(submission.getAppurl());
            weightedTotal += signal.getScore() * strategy.getWeight();
            weightSum += strategy.getWeight();

            riskAssessmentRepository.save(RiskAssessment.builder()
                    .appSubmission(submission)
                    .strategyName(strategy.getName())
                    .score(signal.getScore())
                    .weight(strategy.getWeight())
                    .detail(signal.getDetail())
                    .build());

            signals.append(strategy.getName()).append(": ").append(signal.getDetail()).append(" | ");
        }

        int finalScore = weightSum == 0 ? 0 : (int) Math.round(weightedTotal / weightSum);
        String level = finalScore >= 60 ? "HIGH" : finalScore >= 25 ? "MEDIUM" : "LOW";

        String message = switch (level) {
            case "HIGH" -> "Multiple strong risk signals found - this app should stay blocked.";
            case "MEDIUM" -> "Some risk signals found - recommend manual admin review.";
            default -> "No significant risk signals found.";
        };

        return VerificationResult.builder()
                .riskScore(finalScore)
                .riskLevel(level)
                .message(message)
                .signalsSummary(signals.toString().trim())
                .build();
    }

    /** Used by the admin dashboard's risk-distribution chart. */
    public List<String> strategyNames() {
        return strategies.stream().map(VerificationStrategy::getName).collect(Collectors.toList());
    }
}

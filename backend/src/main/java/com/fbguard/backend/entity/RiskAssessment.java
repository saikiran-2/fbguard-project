package com.fbguard.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores the result of a single VerificationStrategy run against an AppSubmission,
 * so the admin UI can show *why* a score was produced instead of a black-box number.
 */
@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_submission_id", nullable = false)
    private AppSubmission appSubmission;

    @Column(nullable = false)
    private String strategyName;

    private int score; // 0-100 contribution from this strategy

    private double weight; // how much this strategy counts toward the total

    @Column(length = 500)
    private String detail;

    @Builder.Default
    private LocalDateTime checkedAt = LocalDateTime.now();
}

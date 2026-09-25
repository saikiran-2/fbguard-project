package com.fbguard.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column(nullable = false)
    private String appname;

    @Column(nullable = false)
    private String appid;

    @Column(nullable = false, length = 2048)
    private String appurl;

    private String appIconUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    /** Aggregate 0-100 score produced by AppVerificationService (Facade over Strategy checks). */
    @Builder.Default
    private int riskScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.UNKNOWN;

    @Column(length = 1000)
    private String riskSignals; // human-readable summary of which checks fired

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime decidedAt;

    // One app submission can have many individual verification-strategy results
    @OneToMany(mappedBy = "appSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RiskAssessment> assessments = new ArrayList<>();

    public enum Status { PENDING, LICENSED, REJECTED }
    public enum RiskLevel { LOW, MEDIUM, HIGH, UNKNOWN }
}

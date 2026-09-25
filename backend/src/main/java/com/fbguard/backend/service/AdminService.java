package com.fbguard.backend.service;

import com.fbguard.backend.config.CacheConfig;
import com.fbguard.backend.dto.request.BlacklistRequest;
import com.fbguard.backend.dto.response.AppSubmissionResponse;
import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.BlacklistEntry;
import com.fbguard.backend.exception.ResourceNotFoundException;
import com.fbguard.backend.kafka.AppEventProducer;
import com.fbguard.backend.kafka.event.AppDecisionEvent;
import com.fbguard.backend.repository.AppSubmissionRepository;
import com.fbguard.backend.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AppSubmissionRepository appSubmissionRepository;
    private final BlacklistRepository blacklistRepository;
    private final AppEventProducer eventProducer;

    public List<AppSubmissionResponse> getPending() {
        return appSubmissionRepository.findByStatus(AppSubmission.Status.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {CacheConfig.APP_GALLERY_CACHE, CacheConfig.RISK_DISTRIBUTION_CACHE}, allEntries = true)
    public void decide(Long aid, String decision, String adminUsername) {
        AppSubmission submission = appSubmissionRepository.findById(aid)
                .orElseThrow(() -> new ResourceNotFoundException("App not found: " + aid));

        submission.setStatus("APPROVE".equalsIgnoreCase(decision)
                ? AppSubmission.Status.LICENSED : AppSubmission.Status.REJECTED);
        submission.setDecidedAt(LocalDateTime.now());
        appSubmissionRepository.save(submission);

        eventProducer.publishDecision(new AppDecisionEvent(
                submission.getAid(), submission.getAppname(), decision,
                submission.getSubmittedBy().getUsername(), adminUsername, LocalDateTime.now()));
    }

    public List<BlacklistEntry> getBlacklist() {
        return blacklistRepository.findAll();
    }

    @CacheEvict(value = CacheConfig.APP_GALLERY_CACHE, allEntries = true)
    public void addToBlacklist(BlacklistRequest request, String adminUsername) {
        blacklistRepository.findByMalicious(request.getMalicious()).ifPresentOrElse(
                existing -> { /* already present, no-op */ },
                () -> blacklistRepository.save(BlacklistEntry.builder()
                        .malicious(request.getMalicious())
                        .addedBy(adminUsername)
                        .build())
        );
    }

    private AppSubmissionResponse toResponse(AppSubmission a) {
        return AppSubmissionResponse.builder()
                .aid(a.getAid())
                .appname(a.getAppname())
                .appid(a.getAppid())
                .appurl(a.getAppurl())
                .appIconUrl(a.getAppIconUrl())
                .username(a.getSubmittedBy().getUsername())
                .status(a.getStatus().name())
                .riskScore(a.getRiskScore())
                .riskLevel(a.getRiskLevel().name())
                .riskSignals(a.getRiskSignals())
                .createdAt(a.getCreatedAt())
                .build();
    }
}

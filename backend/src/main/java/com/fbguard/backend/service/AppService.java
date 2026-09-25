package com.fbguard.backend.service;

import com.fbguard.backend.config.CacheConfig;
import com.fbguard.backend.dto.request.AddAppRequest;
import com.fbguard.backend.dto.response.AppSubmissionResponse;
import com.fbguard.backend.dto.response.VerificationResult;
import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.exception.ResourceNotFoundException;
import com.fbguard.backend.kafka.AppEventProducer;
import com.fbguard.backend.kafka.event.AppSubmittedEvent;
import com.fbguard.backend.repository.AppSubmissionRepository;
import com.fbguard.backend.repository.UserRepository;
import com.fbguard.backend.service.verification.AppVerificationService;
import com.fbguard.backend.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppSubmissionRepository appSubmissionRepository;
    private final UserRepository userRepository;
    private final AppVerificationService verificationService;
    private final FileStorageService fileStorageService;
    private final AppEventProducer eventProducer;

    @CacheEvict(value = CacheConfig.APP_GALLERY_CACHE, allEntries = true)
    public VerificationResult submit(String username, AddAppRequest request, MultipartFile icon) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String iconUrl = fileStorageService.store(icon, "app-icons");

        AppSubmission submission = appSubmissionRepository.save(AppSubmission.builder()
                .appname(request.getAppname())
                .appid(request.getAppid())
                .appurl(request.getAppurl())
                .appIconUrl(iconUrl)
                .submittedBy(user)
                .status(AppSubmission.Status.PENDING)
                .build());

        // Facade call: runs every VerificationStrategy and persists an explainable audit trail.
        VerificationResult result = verificationService.verify(submission);

        submission.setRiskScore(result.getRiskScore());
        submission.setRiskLevel(AppSubmission.RiskLevel.valueOf(result.getRiskLevel()));
        submission.setRiskSignals(result.getSignalsSummary());

        // Low risk auto-licenses, high risk auto-rejects, medium waits for a human.
        if ("LOW".equals(result.getRiskLevel())) {
            submission.setStatus(AppSubmission.Status.LICENSED);
            submission.setDecidedAt(LocalDateTime.now());
        } else if ("HIGH".equals(result.getRiskLevel())) {
            submission.setStatus(AppSubmission.Status.REJECTED);
            submission.setDecidedAt(LocalDateTime.now());
        }
        appSubmissionRepository.save(submission);

        // Publish for decoupled audit/notification consumers (event-driven side of the design).
        eventProducer.publishSubmitted(new AppSubmittedEvent(
                submission.getAid(), submission.getAppname(), submission.getAppurl(),
                username, result.getRiskScore(), result.getRiskLevel(), LocalDateTime.now()));

        return result;
    }

    /**
     * Cached because the gallery is read far more often than it changes -
     * repeated page loads within the cache window (see CacheConfig, 30s)
     * never touch the database. Paginated because returning every row ever
     * submitted stops being reasonable once there are thousands of them.
     */
    @Cacheable(value = CacheConfig.APP_GALLERY_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AppSubmissionResponse> getGallery(Pageable pageable) {
        return appSubmissionRepository.findAll(pageable).map(this::toResponse);
    }

    public List<AppSubmissionResponse> getMine(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return appSubmissionRepository.findBySubmittedBy(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

package com.fbguard.backend.service;

import com.fbguard.backend.config.CacheConfig;
import com.fbguard.backend.dto.response.DashboardStatsResponse;
import com.fbguard.backend.dto.response.RiskBucketResponse;
import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.repository.AppSubmissionRepository;
import com.fbguard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppSubmissionRepository appSubmissionRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    public DashboardStatsResponse getStats(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<AppSubmission> mine = appSubmissionRepository.findBySubmittedBy(user);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("LICENSED", mine.stream().filter(a -> a.getStatus() == AppSubmission.Status.LICENSED).count());
        byStatus.put("PENDING", mine.stream().filter(a -> a.getStatus() == AppSubmission.Status.PENDING).count());
        byStatus.put("REJECTED", mine.stream().filter(a -> a.getStatus() == AppSubmission.Status.REJECTED).count());

        List<DashboardStatsResponse.DayCount> perDay = last7Days().stream()
                .map(day -> DashboardStatsResponse.DayCount.builder()
                        .day(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .count(mine.stream().filter(a -> a.getCreatedAt().toLocalDate().equals(day)).count())
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .username(username)
                .totalApps(mine.size())
                .licensedApps(byStatus.get("LICENSED"))
                .blockedApps(byStatus.get("REJECTED"))
                .friendCount(friendService.countFriends(username))
                .appsByStatus(byStatus)
                .submissionsPerDay(perDay)
                .build();
    }

    @Cacheable(CacheConfig.RISK_DISTRIBUTION_CACHE)
    public List<RiskBucketResponse> getRiskDistribution() {
        List<AppSubmission> all = appSubmissionRepository.findAll();
        long low = all.stream().filter(a -> a.getRiskScore() < 25).count();
        long medium = all.stream().filter(a -> a.getRiskScore() >= 25 && a.getRiskScore() < 60).count();
        long high = all.stream().filter(a -> a.getRiskScore() >= 60).count();

        return List.of(
                RiskBucketResponse.builder().bucket("Low (0-24)").count(low).build(),
                RiskBucketResponse.builder().bucket("Medium (25-59)").count(medium).build(),
                RiskBucketResponse.builder().bucket("High (60-100)").count(high).build()
        );
    }

    private List<LocalDate> last7Days() {
        LocalDate today = LocalDateTime.now().toLocalDate();
        return java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> today.minusDays(6 - i))
                .collect(Collectors.toList());
    }
}

package com.fbguard.backend.repository;

import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppSubmissionRepository extends JpaRepository<AppSubmission, Long> {
    List<AppSubmission> findByStatus(AppSubmission.Status status);
    List<AppSubmission> findBySubmittedBy(User user);
    long countByStatus(AppSubmission.Status status);
    long countBySubmittedBy(User user);
    List<AppSubmission> findByCreatedAtAfter(LocalDateTime after);
}

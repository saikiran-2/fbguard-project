package com.fbguard.backend.repository;

import com.fbguard.backend.entity.AppSubmission;
import com.fbguard.backend.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByAppSubmission(AppSubmission appSubmission);
}

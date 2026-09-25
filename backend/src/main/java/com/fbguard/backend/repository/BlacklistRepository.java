package com.fbguard.backend.repository;

import com.fbguard.backend.entity.BlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<BlacklistEntry, Long> {
    List<BlacklistEntry> findAll();
    Optional<BlacklistEntry> findByMalicious(String malicious);
}

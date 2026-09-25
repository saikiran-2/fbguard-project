package com.fbguard.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid;

    @Column(nullable = false, unique = true, length = 2048)
    private String malicious;

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

    private String addedBy;
}

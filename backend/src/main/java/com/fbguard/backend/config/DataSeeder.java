package com.fbguard.backend.config;

import com.fbguard.backend.entity.User;
import com.fbguard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Replaces the original project's hardcoded admin/admin credentials.
 * The admin account is created once on first startup from environment
 * variables, with a BCrypt-hashed password - never checked into source.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email:admin@fbguard.local}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .role(User.Role.ADMIN)
                .country("N/A")
                .phoneno("N/A")
                .build();
        userRepository.save(admin);
        log.info("Seeded admin account '{}'. Change ADMIN_PASSWORD in production if you haven't already.", adminUsername);
    }
}

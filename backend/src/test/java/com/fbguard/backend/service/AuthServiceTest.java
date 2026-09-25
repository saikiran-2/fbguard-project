package com.fbguard.backend.service;

import com.fbguard.backend.dto.request.RegisterRequest;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.exception.DuplicateResourceException;
import com.fbguard.backend.repository.UserRepository;
import com.fbguard.backend.security.JwtUtil;
import com.fbguard.backend.util.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateUsername_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("Password1");
        request.setEmail("alice@example.com");
        request.setCountry("India");
        request.setPhoneno("9000000000");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, null))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_newUser_hashesPasswordBeforeSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("bob");
        request.setPassword("PlainTextPassword1");
        request.setEmail("bob@example.com");
        request.setCountry("India");
        request.setPhoneno("9000000001");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("PlainTextPassword1")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(request, null);

        // The critical security assertion: we NEVER persist the plaintext password.
        assertThat(saved.getPassword()).isEqualTo("$2a$10$hashedvalue");
        assertThat(saved.getPassword()).isNotEqualTo("PlainTextPassword1");
        assertThat(saved.getRole()).isEqualTo(User.Role.USER);
    }
}

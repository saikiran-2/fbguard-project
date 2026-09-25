package com.fbguard.backend.service;

import com.fbguard.backend.dto.request.LoginRequest;
import com.fbguard.backend.dto.request.RegisterRequest;
import com.fbguard.backend.dto.response.AuthResponse;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.exception.DuplicateResourceException;
import com.fbguard.backend.repository.UserRepository;
import com.fbguard.backend.security.JwtUtil;
import com.fbguard.backend.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    public User register(RegisterRequest request, MultipartFile photo) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        String photoUrl = fileStorageService.store(photo, "profiles");

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // never store plaintext
                .email(request.getEmail())
                .gender(request.getGender())
                .country(request.getCountry())
                .phoneno(request.getPhoneno())
                .photoUrl(photoUrl)
                .role(User.Role.USER)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserSummary.builder()
                        .uid(user.getUid())
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .photoUrl(user.getPhotoUrl())
                        .build())
                .build();
    }
}

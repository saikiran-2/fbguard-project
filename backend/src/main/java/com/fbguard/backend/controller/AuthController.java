package com.fbguard.backend.controller;

import com.fbguard.backend.dto.request.LoginRequest;
import com.fbguard.backend.dto.request.RegisterRequest;
import com.fbguard.backend.dto.response.AuthResponse;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest request,
                                       @RequestParam(value = "file", required = false) MultipartFile file) {
        User user = authService.register(request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of("message", "Account created. You can now sign in.", "username", user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

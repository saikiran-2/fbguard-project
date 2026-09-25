package com.fbguard.backend.controller;

import com.fbguard.backend.dto.request.UpdateProfileRequest;
import com.fbguard.backend.dto.response.UserProfileResponse;
import com.fbguard.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse me(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public UserProfileResponse updateMe(Authentication auth,
                                         @Valid @ModelAttribute UpdateProfileRequest request,
                                         @RequestParam(value = "file", required = false) MultipartFile file) {
        return userService.updateProfile(auth.getName(), request, file);
    }
}

package com.fbguard.backend.service;

import com.fbguard.backend.dto.request.UpdateProfileRequest;
import com.fbguard.backend.dto.response.UserProfileResponse;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.exception.ResourceNotFoundException;
import com.fbguard.backend.repository.UserRepository;
import com.fbguard.backend.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public UserProfileResponse getProfile(String username) {
        User user = getByUsername(username);
        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request, MultipartFile photo) {
        User user = getByUsername(username);
        user.setEmail(request.getEmail());
        user.setCountry(request.getCountry());
        user.setPhoneno(request.getPhoneno());

        if (photo != null && !photo.isEmpty()) {
            user.setPhotoUrl(fileStorageService.store(photo, "profiles"));
        }

        return toProfileResponse(userRepository.save(user));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .gender(user.getGender())
                .country(user.getCountry())
                .phoneno(user.getPhoneno())
                .photoUrl(user.getPhotoUrl())
                .role(user.getRole().name())
                .build();
    }
}

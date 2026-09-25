package com.fbguard.backend.controller;

import com.fbguard.backend.dto.request.AddAppRequest;
import com.fbguard.backend.dto.response.AppSubmissionResponse;
import com.fbguard.backend.dto.response.VerificationResult;
import com.fbguard.backend.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
@Tag(name = "Apps", description = "Submitting and browsing verified Facebook apps")
public class AppController {

    private final AppService appService;

    @Operation(summary = "Paginated app gallery",
            description = "Returns apps page by page instead of the whole table at once. " +
                    "Defaults to 12 per page, sorted newest first.")
    @GetMapping
    public Page<AppSubmissionResponse> gallery(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return appService.getGallery(pageable);
    }

    @GetMapping("/mine")
    public List<AppSubmissionResponse> mine(Authentication auth) {
        return appService.getMine(auth.getName());
    }

    @Operation(summary = "Submit an app for verification",
            description = "Runs the Strategy+Facade verification pipeline synchronously and " +
                    "publishes an AppSubmittedEvent to Kafka for async audit/notification. " +
                    "Rate-limited - see RateLimitFilter.")
    @PostMapping(consumes = "multipart/form-data")
    public VerificationResult submit(Authentication auth,
                                      @Valid @ModelAttribute AddAppRequest request,
                                      @RequestParam(value = "icon", required = false) MultipartFile icon) {
        return appService.submit(auth.getName(), request, icon);
    }
}

package com.fbguard.backend.controller;

import com.fbguard.backend.dto.request.AppDecisionRequest;
import com.fbguard.backend.dto.request.BlacklistRequest;
import com.fbguard.backend.dto.response.AppSubmissionResponse;
import com.fbguard.backend.entity.BlacklistEntry;
import com.fbguard.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/apps/pending")
    public List<AppSubmissionResponse> pending() {
        return adminService.getPending();
    }

    @PostMapping("/apps/{aid}/decision")
    public void decide(Authentication auth, @PathVariable Long aid, @Valid @RequestBody AppDecisionRequest request) {
        adminService.decide(aid, request.getDecision(), auth.getName());
    }

    @GetMapping("/blacklist")
    public List<BlacklistEntry> blacklist() {
        return adminService.getBlacklist();
    }

    @PostMapping("/blacklist")
    public void addBlacklist(Authentication auth, @Valid @RequestBody BlacklistRequest request) {
        adminService.addToBlacklist(request, auth.getName());
    }
}

package com.fbguard.backend.controller;

import com.fbguard.backend.dto.request.FriendRequestDto;
import com.fbguard.backend.dto.request.FriendResponseDto;
import com.fbguard.backend.dto.response.FriendDto;
import com.fbguard.backend.dto.response.PendingRequestDto;
import com.fbguard.backend.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public List<FriendDto> friends(Authentication auth) {
        return friendService.getFriends(auth.getName());
    }

    @GetMapping("/pending")
    public List<PendingRequestDto> pending(Authentication auth) {
        return friendService.getPending(auth.getName());
    }

    @PostMapping("/request")
    public void request(Authentication auth, @Valid @RequestBody FriendRequestDto dto) {
        friendService.sendRequest(auth.getName(), dto.getRto());
    }

    @PostMapping("/respond")
    public void respond(Authentication auth, @Valid @RequestBody FriendResponseDto dto) {
        friendService.respond(auth.getName(), dto.getRfrom(), dto.getStatus());
    }
}

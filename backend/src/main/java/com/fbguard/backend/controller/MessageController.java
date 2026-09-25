package com.fbguard.backend.controller;

import com.fbguard.backend.dto.request.SendMessageRequest;
import com.fbguard.backend.dto.response.MessageResponse;
import com.fbguard.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{username}")
    public List<MessageResponse> thread(Authentication auth, @PathVariable String username) {
        return messageService.getThread(auth.getName(), username);
    }

    @PostMapping
    public void send(Authentication auth, @Valid @RequestBody SendMessageRequest request) {
        messageService.send(auth.getName(), request.getMsgto(), request.getMsg());
    }
}

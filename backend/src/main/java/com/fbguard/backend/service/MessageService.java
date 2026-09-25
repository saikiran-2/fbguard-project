package com.fbguard.backend.service;

import com.fbguard.backend.dto.response.MessageResponse;
import com.fbguard.backend.entity.Message;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.exception.ResourceNotFoundException;
import com.fbguard.backend.repository.MessageRepository;
import com.fbguard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void send(String fromUsername, String toUsername, String text) {
        User from = userRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + fromUsername));
        User to = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + toUsername));

        Message saved = messageRepository.save(Message.builder().sender(from).receiver(to).msg(text).build());

        MessageResponse payload = MessageResponse.builder()
                .mid(saved.getMid())
                .msgfrom(from.getUsername())
                .msgto(to.getUsername())
                .msg(saved.getMsg())
                .sentAt(saved.getSentAt())
                .build();

        // Instant delivery: if the recipient has an open WebSocket connection
        // (see WebSocketConfig + WebSocketAuthInterceptor), this lands in their
        // browser immediately instead of waiting for their next REST poll.
        // If they're offline, this simply has no subscriber - no error, no
        // retry needed, since getThread() below is still the source of truth
        // they'll see next time they open the chat.
        messagingTemplate.convertAndSendToUser(toUsername, "/queue/messages", payload);
    }

    public List<MessageResponse> getThread(String username, String otherUsername) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        User other = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + otherUsername));

        return messageRepository
                .findBySenderAndReceiverOrReceiverAndSenderOrderBySentAtAsc(user, other, user, other)
                .stream()
                .map(m -> MessageResponse.builder()
                        .mid(m.getMid())
                        .msgfrom(m.getSender().getUsername())
                        .msgto(m.getReceiver().getUsername())
                        .msg(m.getMsg())
                        .sentAt(m.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }
}

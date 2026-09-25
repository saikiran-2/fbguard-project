
package com.fbguard.backend.service;

import com.fbguard.backend.dto.response.FriendDto;
import com.fbguard.backend.dto.response.PendingRequestDto;
import com.fbguard.backend.entity.FriendRequest;
import com.fbguard.backend.entity.User;
import com.fbguard.backend.exception.DuplicateResourceException;
import com.fbguard.backend.exception.ResourceNotFoundException;
import com.fbguard.backend.repository.FriendRequestRepository;
import com.fbguard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public void sendRequest(String fromUsername, String toUsername) {
        if (fromUsername.equalsIgnoreCase(toUsername)) {
            throw new IllegalArgumentException(
                    "You cannot send a friend request to yourself"
            );
        }

        User from = userRepository.findByUsername(fromUsername)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + fromUsername
                        )
                );

        User to = userRepository.findByUsername(toUsername)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + toUsername
                        )
                );

        friendRequestRepository.findByFromUserAndToUser(from, to)
                .ifPresent(r -> {
                    throw new DuplicateResourceException(
                            "A friend request already exists between these users"
                    );
                });

        friendRequestRepository.save(
                FriendRequest.builder()
                        .fromUser(from)
                        .toUser(to)
                        .status(FriendRequest.Status.PENDING)
                        .build()
        );
    }

    public void respond(String toUsername, String fromUsername, String decision) {
        User to = userRepository.findByUsername(toUsername)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + toUsername
                        )
                );

        User from = userRepository.findByUsername(fromUsername)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + fromUsername
                        )
                );

        FriendRequest request =
                friendRequestRepository.findByFromUserAndToUser(from, to)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No pending request from " + fromUsername
                                )
                        );

        request.setStatus(
                "Accept".equalsIgnoreCase(decision)
                        ? FriendRequest.Status.ACCEPTED
                        : FriendRequest.Status.REJECTED
        );

        friendRequestRepository.save(request);
    }

    public List<PendingRequestDto> getPending(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        )
                );

        return friendRequestRepository
                .findByToUserAndStatus(
                        user,
                        FriendRequest.Status.PENDING
                )
                .stream()
                .map(r ->
                        PendingRequestDto.builder()
                                .rfrom(r.getFromUser().getUsername())
                                .build()
                )
                .collect(Collectors.toList());
    }

    public List<FriendDto> getFriends(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        )
                );

        List<FriendDto> asRequester =
                friendRequestRepository
                        .findByFromUserAndStatus(
                                user,
                                FriendRequest.Status.ACCEPTED
                        )
                        .stream()
                        .map(r -> toFriendDto(r.getToUser()))
                        .collect(Collectors.toList());

        List<FriendDto> asReceiver =
                friendRequestRepository
                        .findByToUserAndStatus(
                                user,
                                FriendRequest.Status.ACCEPTED
                        )
                        .stream()
                        .map(r -> toFriendDto(r.getFromUser()))
                        .collect(Collectors.toList());

        return java.util.stream.Stream.concat(
                        asRequester.stream(),
                        asReceiver.stream()
                )
                .collect(Collectors.toList());
    }

    public long countFriends(String username) {
        return getFriends(username).size();
    }

    private FriendDto toFriendDto(User u) {
        return FriendDto.builder()
                .username(u.getUsername())
                .photoUrl(u.getPhotoUrl())
                .build();
    }
}


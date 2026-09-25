package com.fbguard.backend.repository;

import com.fbguard.backend.entity.FriendRequest;
import com.fbguard.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByToUserAndStatus(User toUser, FriendRequest.Status status);
    List<FriendRequest> findByFromUserAndStatus(User fromUser, FriendRequest.Status status);
    Optional<FriendRequest> findByFromUserAndToUser(User fromUser, User toUser);
}

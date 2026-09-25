package com.fbguard.backend.repository;

import com.fbguard.backend.entity.Message;
import com.fbguard.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderBySentAtAsc(
            User sender1, User receiver1, User receiver2, User sender2);
}

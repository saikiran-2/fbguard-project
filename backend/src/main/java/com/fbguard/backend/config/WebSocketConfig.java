package com.fbguard.backend.config;

import com.fbguard.backend.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Real-time chat over STOMP/WebSocket, replacing the frontend's old
 * poll-the-REST-endpoint-every-few-seconds approach with instant delivery.
 *
 * - Clients connect to /ws (SockJS fallback for browsers/networks that block
 *   raw WebSockets).
 * - "/app/**" is where clients SEND messages to the server.
 * - "/topic/**" and "/user/**" are where the server PUSHES messages back out;
 *   Spring's simple in-memory broker handles routing (fine at this scale -
 *   a production system with multiple backend instances would swap this for
 *   a real broker like RabbitMQ, the same upgrade path as Caffeine -> Redis).
 * - convertAndSendToUser() in MessageService relies on the Principal set by
 *   WebSocketAuthInterceptor below to know which specific connected user to
 *   deliver a message to.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}

package com.fpt.config;// WebSocketConfig.java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${frontend.url}")
    private String frontendUrl;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); //  client send subscribe
        config.setApplicationDestinationPrefixes("/app"); // if client send (push from server)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint connect websocket
                .setAllowedOriginPatterns(frontendUrl)
                .withSockJS(); // support SockJS (fallback if no support)
    }
}

package com.fpt.config;// WebSocketConfig.java
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // nơi client sẽ subscribe
        config.setApplicationDestinationPrefixes("/app"); // nếu client gửi lên (không cần dùng nếu chỉ push từ server)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint kết nối websocket
                .setAllowedOriginPatterns("http://localhost:3001")
                .withSockJS(); // hỗ trợ SockJS (fallback nếu không hỗ trợ websocket gốc)
    }
}

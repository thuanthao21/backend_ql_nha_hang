package com.dineflow.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Nơi server gửi tin về (Frontend sẽ lắng nghe ở các đường dẫn bắt đầu bằng /topic)
        config.enableSimpleBroker("/topic");
        // Tiền tố cho các tin nhắn từ Client gửi lên (nếu có dùng)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Mở endpoint "/ws" để ReactJS kết nối vào
        // setAllowedOriginPatterns("*") để tránh lỗi CORS khi chạy localhost khác port
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
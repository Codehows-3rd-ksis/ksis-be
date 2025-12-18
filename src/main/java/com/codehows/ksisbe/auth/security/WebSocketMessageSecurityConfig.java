package com.codehows.ksisbe.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@Configuration
@EnableWebSocketSecurity
public class WebSocketMessageSecurityConfig {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager() {

        MessageMatcherDelegatingAuthorizationManager.Builder builder =
                MessageMatcherDelegatingAuthorizationManager.builder();

        builder
                // 관리자 전용 topic
                .simpSubscribeDestMatchers("/topic/**")
                .hasRole("ADMIN")

                // 유저 개인 큐
                .simpSubscribeDestMatchers("/user/queue/**")
                .authenticated()

                .anyMessage().denyAll();

        return builder.build();
    }
}

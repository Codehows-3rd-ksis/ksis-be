package com.codehows.ksisbe.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //메시지 헤더에서 STOMP 관련 정보를 추출
        StompHeaderAccessor accessor =  MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        //STOMP 커넥트 jwt토큰 재활용
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("웹소켓커넥트 인증헤더: " + authorizationHeader);

            //authorization 헤더, Bearer로 시작하는지 확인
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7); //Bearer 접두사 제거
                //jwt 토큰 유효성 검증
                if (jwtTokenProvider.validateToken(jwt)) {
                    //토큰 유효하면 Authentication 객체 생성, SecurityContext에 설정
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    accessor.setUser(authentication); //웹소켓 세션에 인증된 사용자 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication); //시큐리티 컨택스트에 인증정보저장
                    log.info("웹소켓커넥트 인증정보: " + authentication.getName());
                } else {
                    log.info("웹소켓커넥트: 유효하지 않은 토큰");
                    throw new RuntimeException("유효하지 않은 토큰");
                }
            } else {
                log.info("웹소켓커넥트: 인증헤더가 없거나 잘못된 형식, 연결거부");
                throw new RuntimeException("인증헤더가 없거나 잘못된 형식");
            }
        }
        else if (accessor.getUser() != null) {
            SecurityContextHolder.getContext().setAuthentication((Authentication) accessor.getUser());
        }
        else {
            SecurityContextHolder.clearContext(); //인증되지 않은 사용자는 SecurityContext 초기화
        }
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            SecurityContextHolder.clearContext();
            log.info("웹소켓 연결되지 않음: 시큐리티컨택스트 정리");
        }
    }
}
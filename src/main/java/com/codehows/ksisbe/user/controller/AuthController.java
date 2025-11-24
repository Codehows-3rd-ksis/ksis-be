package com.codehows.ksisbe.user.controller;

import com.codehows.ksisbe.user.dto.LoginRequestDto;
import com.codehows.ksisbe.user.jwt.TokenInfo;
import com.codehows.ksisbe.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j // 로깅을 위한 Lombok 어노테이션
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다. (의존성 주입)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login") // HTTP POST 요청을 "/api/auth/login" 경로에 매핑합니다.
    public TokenInfo login(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("login request. username={}, password={}", loginRequestDto.getUsername(), loginRequestDto.getPassword());
        TokenInfo tokenInfo = authService.login(loginRequestDto); // AuthService를 통해 로그인 처리 및 토큰 생성
        return tokenInfo;
    }
}

package com.codehows.ksisbe.auth.service;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.auth.dto.LoginRequestDto;
import com.codehows.ksisbe.auth.jwt.JwtTokenProvider;
import com.codehows.ksisbe.auth.jwt.TokenInfo;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenInfo login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsernameAndIsDelete(loginRequestDto.getUsername(), "N")
                .orElseThrow(() -> new BadCredentialsException("유저를 찾을 수 없습니다: "+ loginRequestDto.getUsername()));

        log.info("찾은 유저: {}", user.getUsername());
        log.info("입력된 비밀번호: {}", loginRequestDto.getPassword());
        log.info("DB 비밀번호 (암호화된 상태): {}", user.getPassword());

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(("유효하지 않은 비밀번호입니다."));
        }

        if ("승인대기".equals(user.getState())) {
            throw new BadCredentialsException("승인대기 상태입니다. 관리자의 승인이 필요합니다.");
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        TokenInfo tokenInfo  = jwtTokenProvider.generateToken(authentication);

        user.setLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return tokenInfo;
    }
}

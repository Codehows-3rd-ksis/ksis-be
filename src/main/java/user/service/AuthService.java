package user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import user.User;
import user.dto.LoginRequestDto;
import user.jwt.JwtTokenProvider;
import user.jwt.TokenInfo;
import user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenInfo login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUsernameIsDelete(loginRequestDto.getUsername(), "N")
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다: "+ loginRequestDto.getUsername()));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(("유효하지 않은 비밀번호입니다."));
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        TokenInfo tokenInfo  = jwtTokenProvider.generateToken(authentication);

        user.setLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return tokenInfo;
    }
}

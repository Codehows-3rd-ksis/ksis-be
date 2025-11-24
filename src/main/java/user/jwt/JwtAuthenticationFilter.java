package user.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_URLS = Arrays.asList(


            "/ws/**",
            "/login",
            "/user/**"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Request Header에서 JWT 토큰을 추출합니다.
        String token = resolveToken(request);

        // 추출된 토큰이 유효한지 검사합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효할 경우, 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장합니다.
            // SecurityContextHolder는 현재 스레드의 보안 컨텍스트를 저장하는 역할을 합니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response); // 다음 필터로 요청을 전달합니다.
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7); // "Bearer " (7자) 이후의 문자열이 토큰입니다.
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestUri = request.getRequestURI();
        // EXCLUDE_URLS 목록의 패턴 중 현재 요청 URI와 일치하는 것이 있는지 확인합니다.
        return EXCLUDE_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }
}

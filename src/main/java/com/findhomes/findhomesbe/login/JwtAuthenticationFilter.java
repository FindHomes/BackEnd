package com.findhomes.findhomesbe.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 URL 확인
        String requestURI = request.getRequestURI();

        // /api로 시작하는 경로에 대해서는 필터를 건너뛰도록 설정
        if (requestURI.startsWith("/")) {
            filterChain.doFilter(request, response);
            return;
        }
//        // /api/login 또는 /api/oauth/kakao 경로에 대해서는 필터를 건너뛰도록 설정
//        if (requestURI.equals("/api/login") || requestURI.equals("/api/oauth/kakao") || requestURI.equals("/swagger-ui/index.html")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        // JWT 추출
        String token = extractTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효하다면 사용자 정보 추출
            String userId = jwtTokenProvider.getUserId(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 사용자 ID를 기반으로 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("id:"+userId+" 인증완료");
            }
        } else {
            // 토큰이 유효하지 않으면 401 에러 응답을 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 유효하지 않습니다");
            return;
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }
}

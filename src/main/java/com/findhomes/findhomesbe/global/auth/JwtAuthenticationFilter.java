package com.findhomes.findhomesbe.global.auth;

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
    private final SecurityService securityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 특정 URL 경로에 대해 필터를 건너뛰도록 설정
        if (requestURI.startsWith("/swagger") || requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/swagger-ui") || requestURI.contains("oauth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // JWT 추출
        String token = securityService.extractTokenFromRequest(request);
        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 유효한 토큰이면 사용자 정보 추출
                String userId = jwtTokenProvider.getUserId(token);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                // 유효하지 않은 토큰일 경우
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다");
                return;
            }
        } catch (Exception e) {
            // 예외 발생 시 401 응답 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 처리 중 오류가 발생했습니다.");
            return;
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }


}

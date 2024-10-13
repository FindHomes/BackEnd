package com.findhomes.findhomesbe.login;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.exception.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.findhomes.findhomesbe.controller.MainController.MAN_CON_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {
    private final JwtTokenProvider jwtTokenProvider;

    // 클라이언트 요청 헤더에서 JWT 토큰 추출
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }

    // 기존 세션 무효화 및 새로운 세션 생성
    public void sessionCheck(ManConRequest request, HttpServletRequest httpRequest) {
        // 기존 세션 무효화
        HttpSession existingSession = httpRequest.getSession(false); // 기존 세션이 있을 경우 가져옴
        if (existingSession != null) {
            existingSession.invalidate(); // 기존 세션 무효화
        }
        // 새로운 세션 생성
        HttpSession session = httpRequest.getSession(true);
        String chatSessionId = session.getId();
        log.info("새로운 대화 세션 ID 생성: {}", chatSessionId);
        // 세션에 필터링된 필수 조건 저장
        log.info("입력된 필수 조건: {}", request);
        session.setAttribute(MAN_CON_KEY, request);
    }

    public void validateToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        jwtTokenProvider.validateToken(token);

    }

    public String getUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            throw new UnauthorizedException("JWT 토큰이 첨부되지 않았습니다");
        }
        return jwtTokenProvider.getUserId(token);
    }

    public HttpSession getSession(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false); // 기존 세션을 가져옴
        if (session == null) {
            throw new UnauthorizedException("세션이 유효하지 않습니다.");
        }
        return session;
    }

    public HttpSession getNewSession(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return httpRequest.getSession(true);
    }

    public void addSessionIdOnCookie(String sessionId, HttpServletResponse response) {
        // 쿠키에 세션 ID 추가
        Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);
    }
}

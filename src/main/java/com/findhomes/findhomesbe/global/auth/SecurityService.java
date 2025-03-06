package com.findhomes.findhomesbe.global.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.global.exception.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {
    private final JwtTokenProvider jwtTokenProvider;
    private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보를 가져오는 API의 URL

    // 클라이언트 요청 헤더에서 JWT 토큰 추출
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }

    public String getUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String userId = jwtTokenProvider.getUserId(token);
        return userId;
    }

    public HttpSession getSession(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false); // 기존 세션을 가져옴
        if (session == null) {
            throw new UnauthorizedException("세션이 유효하지 않습니다.");
        }
        return session;
    }

    public HttpSession getNewSession(HttpServletRequest httpRequest) {
        HttpSession currentSession = httpRequest.getSession(false); // 기존 세션을 가져오되 없으면 null 반환
        if (currentSession != null) {
            currentSession.invalidate(); // 기존 세션 무효화
        }

        return httpRequest.getSession(true); // 새로운 세션 생성
    }

    public void addSessionIdOnCookie(String sessionId, HttpServletResponse response) {
        // 쿠키에 세션 ID 추가
        Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);
    }

    // 엑세스 토큰으로 카카오 서버에서 고유 id를 받아오는 함수
    public String getKakaoId(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUrl, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("id").asText(); // 사용자의 카카오 고유 ID 추출
        } catch (Exception e) {
            throw new RuntimeException("카카오에서 고유 id를 얻어오는데 실패하였습니다.", e);
        }
    }
}

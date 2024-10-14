package com.findhomes.findhomesbe.login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.DTO.LoginResponse;
import com.findhomes.findhomesbe.DTO.RedirectResponse;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보를 가져오는 API의 URL

    @GetMapping("/api/oauth/kakao")
    @Operation(summary = "토큰 값 반환", description = "클라이언트로부터 전달된 액세스 토큰을 사용해 카카오 사용자 정보를 조회하고 JWT를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "JWT 토큰을 반환합니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))})
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String accessToken) {
        // 1. 사용자 정보 요청
        String kakaoId = getKakaoId(accessToken);

        // 2. 사용자 조회 및 회원가입 처리
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = new User(kakaoId, "심심한 무지", "kakao", "ACTIVE", LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // 3. JWT 생성
        String jwtToken = jwtTokenProvider.createToken(user.getUserId());
        LoginResponse loginResponse = new LoginResponse(true, 200, "토큰값을 성공적으로 반환하였습니다.", new LoginResponse.JwtToken(jwtToken, "Bearer", "3600000"));

        // 4. 클라이언트에 JWT 반환
        return ResponseEntity.ok(loginResponse);
    }

    // 엑세스 토큰으로 카카오 서버에서 고유 id를 받아오는 함수
    private String getKakaoId(String accessToken) {
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

    @GetMapping("/api/test/oauth/kakao")
    @Operation(summary = "테스트 토큰 반환", description = "테스트 환경에서 바로 JWT를 반환합니다.")
    public ResponseEntity<LoginResponse> testKakaoCallback() {
        // 1. 테스트용 사용자 조회 또는 저장
        User user = userRepository.findByKakaoId("testKakaoId")
                .orElseGet(() -> {
                    User newUser = new User("testKakaoId", "테스트 사용자", "kakao", "ACTIVE", LocalDateTime.now());
                    return userRepository.save(newUser);
                });
        // 2. JWT 생성
        String jwtToken = jwtTokenProvider.createToken(user.getUserId());
        LoginResponse loginResponse = new LoginResponse(true, 200, "테스트 토큰을 성공적으로 반환하였습니다.", new LoginResponse.JwtToken(jwtToken, "Bearer", "36000000"));

        // 3. 헤더에 Bearer 토큰을 담아서 클라이언트에 반환
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);

        return ResponseEntity.ok()
                .headers(headers)  // Bearer 토큰을 헤더에 추가
                .body(loginResponse);  // LoginResponse는 응답 본문에 추가
    }
}

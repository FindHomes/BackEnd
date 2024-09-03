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

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;

    private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보를 가져오는 API의 URL

    @GetMapping("/api/login")
    @Operation(summary = "카카오 로그인 버튼 클릭", description = "카카오 로그인 창 주소를 반환합니다. 로그인을 마치면 /api/oauth/kakao로 리다이렉트 되어 토큰을 반환받습니다.")
    @ApiResponse(responseCode = "200", description = "카카오 로그인을 진행하는 주소를 반환합니다", content = @Content(schema = @Schema(implementation = RedirectResponse.class)))
    public ResponseEntity<RedirectResponse> login() {
        String kakaoAuthUrl = UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();
        RedirectResponse redirectResponse = new RedirectResponse(true,200,"해당 주소로 리다이렉트를 진행합니다",kakaoAuthUrl);
        return ResponseEntity.ok(redirectResponse);
    }

    @GetMapping("/api/oauth/kakao")
    @Operation(summary = "토큰 값 반환", description = "카카오 로그인을 마치면 이 주소로 자동으로 리다이렉트 되어 토큰을 반환받습니다. 리다이렉트되면서 자동으로 code값이 설정됩니다.")
    @ApiResponse(responseCode = "200", description = "토큰 값을 반환함", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))})
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. 액세스 토큰 요청
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        String body = UriComponentsBuilder.fromUriString("")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .build()
                .toUriString().substring(1); // 첫 번째 '?' 문자 제거

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        // 2. 액세스 토큰 추출
        String accessToken = extractAccessToken(response.getBody());

        // 3. 사용자 정보 요청
        String kakaoId = getKakaoId(accessToken);

        // 4. 사용자 조회 및 회원가입 처리
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = new User(kakaoId, "임시 닉네임", "kakao", "ACTIVE", LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // 5. JWT 생성
        String jwtToken = jwtTokenProvider.createToken(user.getUserId());
        LoginResponse loginResponse = new LoginResponse(true,200,"토큰값을 성공적으로 반환하였습니다.",new LoginResponse.JwtToken(jwtToken,"Bearer","3600000"));
        // 6. 클라이언트에 JWT 반환
        return ResponseEntity.ok(loginResponse);
    }

    private String extractAccessToken(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract access token", e);
        }
    }

    private String getKakaoId(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUrl, request, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            System.out.println(jsonNode);
            return jsonNode.get("id").asText(); // 사용자의 카카오 고유 ID 추출
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user info from Kakao", e);
        }
    }
}

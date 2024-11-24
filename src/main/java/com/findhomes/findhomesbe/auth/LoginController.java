package com.findhomes.findhomesbe.auth;

import com.findhomes.findhomesbe.DTO.LoginResponse;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class LoginController {
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 5; // 300분
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityService securityService;
    private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me"; // 사용자 정보를 가져오는 API의 URL

    @GetMapping("/api/oauth/kakao")
    @Operation(summary = "토큰 값 반환", description = "클라이언트로부터 전달된 액세스 토큰을 사용해 카카오 사용자 정보를 조회하고 JWT를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "JWT 토큰을 반환합니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))})
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String accessToken) {
        // 1. 사용자 정보 요청
        String kakaoId = securityService.getKakaoId(accessToken);

        // 2. 사용자 조회 및 회원가입 처리
        User user = userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            String randomNickname = "User_" + UUID.randomUUID().toString().substring(0, 8); // 랜덤 닉네임 생성
            User newUser = new User(kakaoId, randomNickname, "kakao", "ACTIVE", LocalDateTime.now());
            return userRepository.save(newUser);
        });

        // 3. JWT 생성
        String jwtAccessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        // 4. 리프레시 토큰 저장
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(7);
        user.updateRefreshToken(jwtRefreshToken, refreshTokenExpiry);
        userRepository.save(user);
        // 5. 응답 객체 생성
        LoginResponse loginResponse = LoginResponse.builder().success(true).code(200).message("토큰이 성공적으로 반환되었습니다.").result(LoginResponse.Tokens.builder().token(jwtAccessToken).refreshToken(jwtRefreshToken).build()).build();
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/api/oauth/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {

        // 1. RefreshToken 유효성 검증
        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }
        // 2. 새로운 AccessToken 생성 및 선택적으로 새로운 RefreshToken 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(7);

        // 리프레시 토큰 업데이트 후 저장
        user.updateRefreshToken(newRefreshToken, refreshTokenExpiry);
        userRepository.save(user);
        // 3. 응답 객체 생성
        LoginResponse loginResponse = LoginResponse.builder().success(true).code(200).message("토큰이 성공적으로 반환되었습니다.").result(LoginResponse.Tokens.builder().token(newAccessToken).refreshToken(refreshToken).build()).build();
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/api/oauth/logout")
    public ResponseEntity<?> logout(@RequestParam String accessToken) {
        // 1. 사용자 정보 요청
        String kakaoId = securityService.getKakaoId(accessToken);
        // 2. 사용자 조회 및 회원가입 처리
        Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.clearRefreshToken(); // 리프레시 토큰 삭제
            userRepository.save(user); // 변경사항 저장
        }
        return ResponseEntity.ok("로그아웃이 성공적으로 처리되었습니다.");
    }

    @GetMapping("/api/oauth/test")
    @Operation(summary = "테스트 토큰 반환", description = "테스트 환경에서 바로 JWT를 반환합니다.")
    public ResponseEntity<LoginResponse> testKakaoCallback() {
        User user = userRepository.findByKakaoId("testKakaoId").orElseGet(() -> {
            User newUser = new User("testKakaoId", "테스트 사용자", "kakao", "ACTIVE", LocalDateTime.now());
            return userRepository.save(newUser);
        });
        String jwtAccessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        LoginResponse loginResponse = LoginResponse.builder().success(true).code(200).message("토큰이 성공적으로 반환되었습니다.").result(LoginResponse.Tokens.builder().token(jwtAccessToken).refreshToken(jwtRefreshToken).build()).build();
        return ResponseEntity.ok(loginResponse);
    }
}

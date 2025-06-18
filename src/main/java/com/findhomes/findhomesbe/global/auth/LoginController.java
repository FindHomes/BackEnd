package com.findhomes.findhomesbe.global.auth;

import com.findhomes.findhomesbe.domain.user.User;
import com.findhomes.findhomesbe.domain.user.UserRepository;
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
import org.springframework.web.server.ResponseStatusException;

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
    private final RefreshTokenRepository refreshTokenRepository;

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
        RefreshToken jwtRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        // 4. 리프레시 토큰 저장
        refreshTokenRepository.save(jwtRefreshToken);
        userRepository.save(user);
        // 5. 응답 객체 생성
        LoginResponse loginResponse = LoginResponse.builder().success(true).code(200).message("토큰이 성공적으로 반환되었습니다.").result(LoginResponse.Tokens.builder().token(jwtAccessToken).refreshToken(jwtRefreshToken).build()).build();
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/api/oauth/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        RefreshToken refreshToken1 = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = userRepository.findById(refreshToken1.getUserId()).orElse(null);


        String newAccessToken = jwtTokenProvider.createAccessToken(refreshToken1.getUserId());
        RefreshToken newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        // 리프레시 토큰 업데이트 후 저장
        refreshTokenRepository.save(newRefreshToken);

        userRepository.save(user);
        // 3. 응답 객체 생성
        LoginResponse loginResponse = LoginResponse.builder().success(true).code(200).message("토큰이 성공적으로 반환되었습니다.").result(LoginResponse.Tokens.builder().token(newAccessToken).refreshToken(newRefreshToken).build()).build();
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/api/oauth/logout")
    public ResponseEntity<?> logout(@RequestParam String accessToken) {
        // 1. 사용자 식별
        String kakaoId = securityService.getKakaoId(accessToken);
        Optional<User> userOpt = userRepository.findByKakaoId(kakaoId);

        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다.");
        }

        // 2. 저장된 Refresh Token 제거
        String userId = userOpt.get().getUserId();
        refreshTokenRepository.deleteById(userId);

        // 3. 응답 반환
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
        RefreshToken jwtRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        LoginResponse loginResponse = LoginResponse.builder().success(true).code(200).message("토큰이 성공적으로 반환되었습니다.").result(LoginResponse.Tokens.builder().token(jwtAccessToken).refreshToken(jwtRefreshToken).build()).build();
        return ResponseEntity.ok(loginResponse);
    }
}

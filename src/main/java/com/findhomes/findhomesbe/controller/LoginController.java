package com.findhomes.findhomesbe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class LoginController {

    // 환경변수에서 값을 읽어옴
    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;



    @GetMapping("/api/login")
    public String login() {
        String kakaoAuthUrl = UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();

        // 클라이언트를 카카오 로그인 페이지로 리다이렉트
        return "redirect:" + kakaoAuthUrl;
    }

    @GetMapping("/api/oauth/kakao")
    public ResponseEntity<String> kakaoCallback(@RequestParam String code) {
        System.out.println("접근시작");
        RestTemplate restTemplate = new RestTemplate();

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
        // Post 로 토큰 발급
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
        System.out.println(response);

        // 클라이언트에 토큰 반환
        return response;
    }
}

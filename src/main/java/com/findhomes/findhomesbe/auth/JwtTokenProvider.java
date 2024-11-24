package com.findhomes.findhomesbe.auth;

import com.findhomes.findhomesbe.exception.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private String secretKey = "Pq/4DfE6881zcauYx+HRkoYNvCdJemvE/65fSYCFSSQ="; // 변경예정 비밀키
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 3; // 60분
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일
    private Key key;

    @PostConstruct
    protected void init() {
        // HMAC SHA256 서명을 위한 비밀 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // JWT 엑세스 토큰 생성
    public String createAccessToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);
        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 리프레시 토큰 생성
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // JWT 토큰의 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 유효한 토큰
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다."); // 만료된 토큰에 대한 로그
            throw new UnauthorizedException("토큰이 만료되었습니다."); // 만료된 토큰 예외 메시지
        } catch (io.jsonwebtoken.SignatureException e) {
            System.out.println("토큰 서명이 유효하지 않습니다."); // 서명 검증 실패
            throw new UnauthorizedException("토큰 서명이 유효하지 않습니다.");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("토큰 형식이 잘못되었습니다."); // 잘못된 형식의 토큰
            throw new UnauthorizedException("토큰 형식이 잘못되었습니다.");
        } catch (Exception e) {
            System.out.println("알 수 없는 이유로 토큰이 유효하지 않습니다.");
            throw new UnauthorizedException("알 수 없는 이유로 토큰이 유효하지 않습니다.");
        }
    }


    public String getUserId(String token) {
        try {
            // JWT 토큰에서 사용자 ID 추출
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("JWT 토큰이 비어있거나 잘못되었습니다.");
        } catch (MalformedJwtException e) {
            throw new UnauthorizedException("잘못된 형식의 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("JWT 토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException("지원되지 않는 JWT 토큰 형식입니다.");
        } catch (SignatureException e) {
            throw new UnauthorizedException("JWT 서명 검증에 실패하였습니다.");
        } catch (Exception e) {
            throw new UnauthorizedException("JWT 토큰을 파싱하는 중 알 수 없는 오류가 발생하였습니다.");
        }
    }

}

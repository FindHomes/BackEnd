package com.findhomes.findhomesbe.login;

import com.findhomes.findhomesbe.exception.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private String secretKey = "Pq/4DfE6881zcauYx+HRkoYNvCdJemvE/65fSYCFSSQ="; // 변경예정 비밀키
    private long validityInMilliseconds = 3600000; // 1 hour in milliseconds

    private Key key;

    @PostConstruct
    protected void init() {
        // HMAC SHA256 서명을 위한 비밀 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // JWT 토큰 생성
    public String createToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰의 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new UnauthorizedException("토큰이 유효하지 않습니다.");
        }
    }

    // JWT 토큰에서 사용자 ID 추출
    public String getUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}

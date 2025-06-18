package com.findhomes.findhomesbe.global.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefreshTokenTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void 리프레시토큰_저장_및_조회_테스트() {
        RefreshToken token = new RefreshToken("sampleToken", "test");
        refreshTokenRepository.save(token);

        Optional<RefreshToken> found = refreshTokenRepository.findById("sampleToken");
        assertTrue(found.isPresent());
        assertEquals("test", found.get().getUserId());
    }
}

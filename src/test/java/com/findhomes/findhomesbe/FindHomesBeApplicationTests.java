package com.findhomes.findhomesbe;

import com.findhomes.findhomesbe.login.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FindHomesBeApplicationTests {


    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Test
    void contextLoads() {
    }

    @Test
    void generateTestToken() {
        // 테스트용 사용자 ID
        String testUserId = "testUser";
        // 토큰 생성
        String testToken = jwtTokenProvider.createToken(testUserId);
        System.out.println("생성된 토큰: " + testToken);
    }
}

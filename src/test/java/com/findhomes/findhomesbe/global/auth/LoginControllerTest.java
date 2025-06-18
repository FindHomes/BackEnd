package com.findhomes.findhomesbe.global.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.domain.user.User;
import com.findhomes.findhomesbe.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private UserRepository userRepository;

    @MockBean
    private SecurityService securityService;

    @Test
    void 카카오_엑세스_토큰으로_JWT_발급() throws Exception {
        // given
        String accessToken = "mockAccessToken";
        String kakaoId = "kakaoMockId_" + UUID.randomUUID();

        when(securityService.getKakaoId(accessToken)).thenReturn(kakaoId);

        // when
        MvcResult result = mockMvc.perform(get("/api/oauth/kakao")
                        .param("accessToken", accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String content = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);

        assertTrue(json.get("success").asBoolean());
        assertEquals(200, json.get("code").asInt());

        JsonNode resultNode = json.get("result");
        assertNotNull(resultNode.get("token").asText());

        JsonNode refreshTokenNode = resultNode.get("refreshToken");
        assertNotNull(refreshTokenNode.get("refreshToken").asText());
        assertNotNull(refreshTokenNode.get("userId").asText());

        // 실제 DB에 저장되었는지 확인
        assertTrue(userRepository.findByKakaoId(kakaoId).isPresent());
        assertTrue(refreshTokenRepository.findById(refreshTokenNode.get("refreshToken").asText()).isPresent());
    }

    @Test
    void 유효한_리프레시_토큰으로_엑세스토큰_재발급_성공() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();

        // DB에 사용자 저장 (refreshToken의 userId와 일치해야 함)
        User user = new User("testKakaoId_" + userId, "테스트유저", "kakao", "ACTIVE", LocalDateTime.now());
        user.setUserId(userId);
        userRepository.save(user);

        // 리프레시 토큰 발급 및 Redis에 저장
        RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenRepository.save(refreshToken);

        // when
        MvcResult result = mockMvc.perform(post("/api/oauth/refresh")
                        .param("refreshToken", refreshToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        assertTrue(json.get("success").asBoolean());
        JsonNode resultNode = json.get("result");
        assertNotNull(resultNode.get("token").asText());

        JsonNode refreshTokenNode = resultNode.get("refreshToken");
        assertNotNull(refreshTokenNode.get("refreshToken").asText());
        assertNotNull(refreshTokenNode.get("userId").asText());
    }
}

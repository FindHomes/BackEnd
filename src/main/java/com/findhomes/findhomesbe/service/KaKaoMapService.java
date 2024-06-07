package com.findhomes.findhomesbe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class KaKaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<double[]> getLocations(String keyword) throws IOException {
        List<double[]> allLocations = new ArrayList<>();
        int page = 1;
        int size = 15;
        int maxPages = 45;

        // 서울 중심 좌표와 반경 설정
        double seoulX = 126.9784; // 경도
        double seoulY = 37.5665;  // 위도
        int radius = 10000; // 10km 반경

        while (page <= maxPages) {
            String url = String.format("https://dapi.kakao.com/v2/local/search/keyword.json?query=%s&page=%d&size=%d&x=%f&y=%f&radius=%d",
                    keyword, page, size, seoulX, seoulY, radius);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("documents")) {
                JsonNode documents = jsonNode.get("documents");
                for (JsonNode document : documents) {
                    double x = document.get("x").asDouble();
                    double y = document.get("y").asDouble();
                    allLocations.add(new double[]{x, y});
                }
            }

            // 더 이상 결과가 없으면 종료
            if (!jsonNode.has("meta") || !jsonNode.get("meta").get("is_end").asBoolean()) {
                break;
            }

            page++;
        }

        return allLocations;
    }
}

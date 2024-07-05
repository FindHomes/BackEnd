package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.service.ChatGPTService;
import com.findhomes.findhomesbe.service.HouseService;
import com.findhomes.findhomesbe.service.KaKaoMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ChatGPTService chatGPTService;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;

    @PostMapping("/api/search")
    public ResponseEntity<Map<String, List<SearchResponse.Response.Ranking>>> search(@RequestBody SearchRequest request) throws IOException {
        // 유저 입력 및 매물 조건 추출
        String userInput = extractUserInput(request);

        // GPT API 호출
        Map<String, Double> weights = getWeightsFromGPT(userInput);

        // 매물 데이터 가져오기 (임시 데이터, 실제론 매물 데이터 받아야함)
        List<House> houses = houseService.getHouse(request);

        // 시설 좌표 데이터들 가져오기 (아래 예시는 버거킹)
        List<double[]> Locations = kaKaoMapService.getLocations("버거킹");

        // 점수 계산 및 정렬
        List<House> scoredHouses = calculateAndSort(houses, weights, Locations);

        // 변환 및 반환
        List<SearchResponse.Response.Ranking> rankings = houseService.convertToRanking(scoredHouses);

        return new ResponseEntity<>(Map.of("rankings", rankings), HttpStatus.OK);
    }

    private String extractUserInput(SearchRequest request) {
        return request.getUserInput();
    }

    private Map<String, Double> getWeightsFromGPT(String userInput) throws IOException {
        String keywords = keyword();
        String command = createGPTCommand(userInput, keywords);

        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a helpful assistant.")
                        .build(),
                CompletionRequestDto.Message.builder()
                        .role("user")
                        .content(command)
                        .build()
        );

        CompletionRequestDto completionRequestDto = CompletionRequestDto.builder()
                .messages(messages)
                .temperature(0.7)
                .build();

        Map<String, Object> result = chatGPTService.prompt(completionRequestDto);
        System.out.println(result);

        return parseGPTResponse(result);
    }

    private String createGPTCommand(String userInput, String keywords) {
        return "유저의 입력 문장과 현재 우리가 보유한 데이터 목록을 너한테 줄꺼야. 너는 문장과 연관이 있는 데이터를 선정한 뒤 각 데이터에 가중치를 설정하고 그 결과를 나한테 반환해주면 돼. 예를 들어 '집 가까이 버거킹이 있고 치안이 좋았으면 좋겠어' 라고 문장이 들어오면 버거킹0.4-안전0.6 이렇게 반환해주면 돼. 앞에서 말한 반환양식대로만 문장을 반환해주면 돼. '다음은 반환문장입니다'와 같은 미사어구 넣지마. 다음 문장은 유저입력과 보유 데이터야.\n유저 입력문장:" + userInput + "\n보유 데이터:" + keywords;
    }

    private Map<String, Double> parseGPTResponse(Map<String, Object> result) {
        // 예제 파싱 로직, 실제 구현 시 적절한 파싱 로직을 작성
        Map<String, Double> weights = new HashMap<>();
        weights.put("버거킹", 0.4);
        weights.put("안전", 0.6);
        return weights;
    }

    private List<House> getSampleHouses() {
        return Arrays.asList(
                House.builder()
                        .houseId(12345678)
                        .url("https://kustaurant.com")
                        .priceType("mm")
                        .price(20000)
                        .priceForWs(0)
                        .housingType("원룸")
                        .isMultiLayer(false)
                        .isSeparateType(false)
                        .floor("3층")
                        .size(40d)
                        .roomNum(1)
                        .washroomNum(1)
                        .direction("남동")
                        .completionDate(LocalDate.now())
                        .houseOption("에어컨")
                        .address("경기도 안양시 동안구")
                        .x(127.0)
                        .y(37.4)
                        .build(),
                House.builder()
                        .houseId(12345678)
                        .url("https://kustaurant.com")
                        .priceType("mm")
                        .price(20000)
                        .priceForWs(0)
                        .housingType("원룸")
                        .isMultiLayer(false)
                        .isSeparateType(false)
                        .floor("3층")
                        .size(40d)
                        .roomNum(1)
                        .washroomNum(1)
                        .direction("남동")
                        .completionDate(LocalDate.now())
                        .houseOption("에어컨")
                        .address("경기도 안양시 동안구")
                        .x(127.0)
                        .y(37.4)
                        .build()
        );
    }

    private List<House> calculateAndSort(List<House> houses, Map<String, Double> weights, List<double[]> Locations) {
        for (House house : houses) {
            double score = 0.0;
            double minDistance = Double.MAX_VALUE;

            for (double[] location : Locations) {
                double distance = calculateDistance(house.getX(), house.getY(), location[0], location[1]);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            score += (1 / (minDistance + 1)) * weights.getOrDefault("버거킹", 0.0); // 거리 반비례 점수 계산
            if (house.getAddress().contains("안전")) {
                score += weights.getOrDefault("안전", 0.0);
            }
            house.setScore(score);
        }

        houses.sort(Comparator.comparingDouble(House::getScore).reversed());

        return houses;
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        double theta = x1 - x2;
        double dist = Math.sin(deg2rad(y1)) * Math.sin(deg2rad(y2)) + Math.cos(deg2rad(y1)) * Math.cos(deg2rad(y2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344; // km 단위
        return dist;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public String keyword() {
        return "버거킹,안전";
    }
}

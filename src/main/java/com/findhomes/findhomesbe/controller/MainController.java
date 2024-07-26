package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.service.ChatGPTService;
import com.findhomes.findhomesbe.service.HospitalService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ChatGPTService chatGPTService;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;


    @PostMapping("/api/search")
    public ResponseEntity<Map<String, List<SearchResponse.Response.Ranking>>> search(@RequestBody SearchRequest request) throws IOException {

        // 1. 키워드 및 가중치 선정
        Map<String, Double> weights = getKeywordANDWeightsFromGPT(request.getUserInput());
        // 2. 매물 데이터 가져오기
        List<House> houses = houseService.getHouse(request);
        // 3. 시설의 좌표 가져오기
        List<double[]> Locations = getLocation(weights);
        // 4. 점수 계산
        List<House> scoredHouses = calculateScore(houses, weights, Locations);
        // 5. 변환 및 반환
        List<SearchResponse.Response.Ranking> rankings = houseService.convertToRanking(scoredHouses);

        return new ResponseEntity<>(Map.of("rankings", rankings), HttpStatus.OK);
    }

    private List<double[]> getLocation(Map<String, Double> weights) {
        List<double[]> allLocations = new ArrayList<>();

        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            String keyword = entry.getKey();

            // 각 키워드에 대해 위치 정보를 가져오는 서비스 호출
            List<double[]> locations = new ArrayList<>();
            switch (keyword) {
                // to do : 위치 가져오기
//                case "음식점":
//                    locations = restaurantService.getAllRestaurantLocations();
//                    break;
//                case "피시방":
//                    locations = pcRoomService.getAllPCRoomLocations();
//                    break;
//                case "미용실":
//                    locations = HairSalonService.getAllHairSalonLocations();
//                    break;
//                case "병원":
//                    locations = hospitalService.getAllHospitalLocations("병원");
//                    break;
//                // 다른 키워드에 대해 추가
                // case "다른키워드":
                //     locations = someOtherService.getLocationsForKeyword("다른키워드");
                //     break;
            }

            allLocations.addAll(locations);
        }

        return allLocations;
    }

    private Map<String, Double> getKeywordANDWeightsFromGPT(String userInput) throws IOException {
        String keywords = keyword();
        String command = createGPTCommand(userInput, keywords);

        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a machine that returns responses according to a predetermined format. Return the result in the specified format without any extra text.")
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
        return String.format(
                "유저 입력 문장: '%s'. 보유 데이터: '%s'. " +
                "유저의 요구사항과 직접적으로 관련된 데이터만을 선정하고, 각 데이터에 가중치를 설정해 한 줄로 반환하세요. " +
                "반환 형식: '음식점0.2,피시방0.2,미용실0.2,병원0.4'. " +   // 롯데타워,건대, 네이버 본사 // 직장, 친구집
                "가중치의 총합은 1이어야 하며, 불필요한 미사어구는 포함하지 마세요. " +
                "포함 관계가 있다면 더 구체적인 키워드에 가중치를 설정하세요.", //
                userInput, keywords
        );
    }




    private Map<String, Double> parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");

        // 정규 표현식을 사용하여 항목과 값을 추출합니다.
        Pattern pattern = Pattern.compile("([^,]+?)(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(content);

        Map<String, Double> parsedData = new HashMap<>();

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Double value = Double.parseDouble(matcher.group(2));
            parsedData.put(key, value);
        }
        return parsedData;
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

    private List<House> calculateScore(List<House> houses, Map<String, Double> weights, List<double[]> Locations) {
        // to do : 매물 점수 계산하기
        return houses;
    }
    // 거리 계산 함수
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
        return "음식점, 미용실, 피시방, 병원, 버거킹, 맥도날드, 노래방, 코인 노래방, 공원, 지하철역, 학교, 쇼핑몰, 카페, 도서관, 은행, 약국, 편의점, 체육관, 영화관, 서점, 수영장, 동물병원, 유치원, 초등학교, 중학교, 대학교, 학원";
    }}

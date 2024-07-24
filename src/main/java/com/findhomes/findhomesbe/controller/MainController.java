package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.UserChatRequest;
import com.findhomes.findhomesbe.DTO.UserChatResponse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Restaurant;
import com.findhomes.findhomesbe.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final ChatGPTService chatGPTService;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;
    private final RestaurantIndustryService restaurantIndustryService;

    private List<House> preHouseData;
    private String userInput;

    @PostMapping("/api/search/man-con")
    @Operation(summary = "필수 조건 입력", description = "필수 조건을 입력하는 api입니다." +
            "\n\nhousingTypes 도메인: \"아파트\", \"원룸\", \"투룸\", \"쓰리룸\", \"쓰리룸 이상\", \"오피스텔\"")
    @ApiResponse(responseCode = "200", description = "챗봇 화면으로 이동해도 좋음.")
    public ResponseEntity<Void> setManConSearch(@RequestBody ManConRequest request) {
        /**
         * [매물 데이터 가져오기]
         * 필수 조건(계약 형태 및 가격, 월세, 집 형태)으로 필터링
         */
        // 필수 입력 조건을 만족하는 매물 리스트 불러오기
        List<House> manConHouses = houseService.getManConHouses(request);
        log.info("매물 개수: {}개", manConHouses.size());

        this.preHouseData = manConHouses;

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/search/user-chat")
    @Operation(summary = "사용자 채팅", description = "사용자 입력을 받고, 챗봇의 응답을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "챗봇 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserChatResponse.class))})
    public ResponseEntity<UserChatResponse> userChat(@RequestBody UserChatRequest userChatRequest) {
        UserChatResponse response = new UserChatResponse();

        this.userInput = userChatRequest.getUserInput();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/search/complete")
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.")
    @ApiResponse(responseCode = "200", description = "매물 응답 완료")
    public ResponseEntity<List<House>> getHouseList() {
        /**
         * [1. 키워드 및 가중치 선정]
         * GPT가 알려줘야 하는 데이터
         * 1) 필수 조건 외에 매물 자체에 대한 추가 조건 (관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션)
         * 2) 필요 시설 (ex. 버거킹, 정형외과, 다이소)
         * 3) 필요 공공 데이터와 가중치
         * 4) 사용자에게 추가로 물어봐야 할 조건?
         */
        Map<String, Double> weights = getKeywordANDWeightsFromGPT(this.userInput);
        log.info("GPT 응답: {}", weights);

        /**
         * [2. 매물 자체 조건으로 매물 필터링]
         * 데이터: 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션
         */


        /**
         * [3. 필요 시설로 매물 필터링하기]
         * 3-1. 필요 시설 가져오기
         * 3-2. 거리 기준으로 매물 필터링하기
         */
        // 3-1. 필요 시설 가져오기
        List<Restaurant> restaurants = restaurantIndustryService.getRestaurantByKeyword(new String[]{"버거"});
        log.info("식당 개수: {}개", restaurants.size());
        // 3-2. 거리 기준으로 매물 필터링하기
        List<House> resultHouses = CoordService.filterHouseByDistance(this.preHouseData, restaurants, 3d);
        log.info("필터링 후 식당 개수: {}개", resultHouses.size());

        /**
         * [4. 매물 점수 계산 및 정렬]
         */

        return new ResponseEntity<>(this.preHouseData, HttpStatus.OK);
    }

    @GetMapping("/api/search/update")
    @Operation(summary = "사용자 지도 상호작용 시 매물 리스트 갱신", description = "사용자가 지도를 움직이거나 확대/축소될 때, 해당 지도에 표시되는 매물 정보를 새로 받아옵니다.")
    @ApiResponse(responseCode = "200", description = "매물 리스트를 반환합니다.")
    public ResponseEntity<List<House>> getUpdatedHouseList(
            @RequestParam @Parameter(description = "경도 최댓값") double xMax,
            @RequestParam @Parameter(description = "경도 최솟값") double xMin,
            @RequestParam @Parameter(description = "위도 최댓값") double yMax,
            @RequestParam @Parameter(description = "위도 최솟값") double yMin
    ) {
        return new ResponseEntity<>(this.preHouseData, HttpStatus.OK);
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


    private Map<String, Double> getKeywordANDWeightsFromGPT(String userInput) {
        String keywords = keyword();
        String command = createGPTCommand(userInput, keywords);

        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a machine that returns responses according to a predetermined format.")
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
        return "유저의 입력 문장과 현재 우리가 보유한 데이터 목록을 너한테 줄꺼야. 너는 문장과 연관이 있는 데이터를 선정한 뒤 각 데이터에 가중치를 설정하고 그 결과를 한줄로 나한테 반환해주면 돼. 예를 들어 '집 가까이 pc방과 음식점, 미용실이 있었으면 좋겠어. 그리고 제일 중요한게 병원이 가까이 있어야해' 라고 문장이 들어오면 [음식점0.2,피시방0.2,미용실0.2, 병원0.4] 이런 양식대로 문장을 반환해야해.  그 관련 데이터에 대한 가중치를 반환하면 되고 관련없으면 0을 반환해. 그리고 가중치를 다 더하면 1이 되어야해.  '다음은 반환문장입니다'와 같은 미사어구 넣지마. 다음 문장은 유저입력과 보유 데이터야. 유저 입력문장:" + userInput + ". 보유 데이터:" + keywords;
    }

    private Map<String, Double> parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");

        // 정규 표현식을 사용하여 항목과 값을 추출합니다.
        Pattern pattern = Pattern.compile("([^,\\[\\]]+?)(\\d+\\.\\d+)");
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
        return "음식점, 미용실, 피시방, 병원";
    }
}

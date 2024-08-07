package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.calculate.CalculateService;
import com.findhomes.findhomesbe.calculate.CoordService;
import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.calculate.data.SafetyEnum;
import com.findhomes.findhomesbe.calculate.SafetyGradeService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
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

import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    public static final double RADIUS = 5d;
    private final ChatGPTService chatGPTService;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;
    private final RestaurantIndustryService restaurantIndustryService;
    private final SafetyGradeService safetyGradeService;

    private List<House> preHouseData = new ArrayList<>();
    private String userInput = "방이 3개이고 화장실 수가 두개였으면 좋겠어. 버거킹이 가깝고, 역세권인 집 찾아줘. 또 나는 중학생인 딸을 키우고 있어. 지역이 학구열이 있었으면 좋겠어";
    private String publicData = "교통사고율,화재율,범죄율,생활안전,자살율,감염병율";
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
    public ResponseEntity<SearchResponse> getHouseList() {
        /**
         * [1. 키워드 및 가중치 선정]
         * GPT가 알려줘야 하는 데이터
         * 1) 필수 조건 외에 매물 자체에 대한 추가 조건 (관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션)
         * 2) 필요 시설 (ex. 버거킹, 정형외과, 다이소)
         * 3) 필요 공공 데이터와 가중치
         * 4) 사용자에게 추가로 물어봐야 할 조건?
         */
        String weights = getKeywordANDWeightsFromGPT(this.userInput);
        log.info("GPT 응답: \n{}", weights);
        List<Map<String, String>> parsingResult = parsingGptResponse(weights);
        for (Map<String, String> stringStringMap : parsingResult) {
            log.info("조건 파싱: {}", stringStringMap);
        }

        /**
         * [2. 매물 자체 조건으로 매물 필터링]
         * 데이터: 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션
         */
        this.preHouseData = houseService.filterByUserInput(parsingResult.get(0), preHouseData);
        log.info("사용자 입력 조건으로 필터링 후 매물 개수: {}개", preHouseData.size());

        /**
         * [3. 필요 시설로 매물 필터링 + 점수 계산]
         * 3-1. 필요 시설 가져오기
         * 3-2. 거리 기준으로 매물 필터링
         * 3-3. 매물에 공공 데이터 정보 넣기
         */
        // 3-1. 필요 시설 가져오기
        Map<String, Double> parsingFacilityMap = new HashMap<>();
        parsingFacilityMap.put("버거킹", 0.1);
        parsingFacilityMap.put("피자헛", 0.2);
        Map<String, List<Industry>> facilitiesMap = new HashMap<>();
        for (String facilityName : parsingFacilityMap.keySet()) {
            facilitiesMap.put(facilityName, restaurantIndustryService.getRestaurantByKeyword(facilityName));
            log.info("시설 {} 개수: {}개", facilityName, facilitiesMap.get(facilityName).size());
        }
        // 3-2. 거리 기준으로 매물 필터링
        List<HouseWithCondition> resultHouseWithConditions = CoordService.filterAndCalculateByFacility(this.preHouseData, facilitiesMap, RADIUS);
        log.info("시설 필터링 후 매물 개수: {}개", resultHouseWithConditions.size());
        // 3-3. 매물에 공공 데이터 정보 넣기
        Map<SafetyEnum, Double> parsingSafetyMap = new HashMap<>();
        parsingResult.get(2).forEach((key, value) -> {
            try {
                parsingSafetyMap.put(SafetyEnum.valueOf(key), Double.parseDouble(value));
            } catch (NumberFormatException e) {
                log.error("GPT응답 파싱에서 공공데이터 가중치가 잘못됨. {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                log.error("GPT응답 파싱에서 공공데이터 이름이 잘못됨. {}", e.getMessage());
            }
        });
        safetyGradeService.insertSafetyGradeInfoInHouseCondition(resultHouseWithConditions, parsingSafetyMap.keySet());

        /**
         * [4. 점수로 매물 정렬 및 반환]
         * 4-1. 점수 계산 및 정렬
         */
        // 4-1. 점수 계산 및 정렬
        List<House> resultHouses = CalculateService.calculateScore(resultHouseWithConditions, parsingFacilityMap, parsingSafetyMap);
        this.preHouseData = resultHouses;
        // 응답 생성 및 반환
        return new ResponseEntity<>(houseService.makeResponse(resultHouses.subList(0, Math.min(100, resultHouses.size()))), HttpStatus.OK);
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
        List<House> updateResponse = this.preHouseData.stream()
                .filter(house -> house.getX() > xMin && house.getX() < xMax && house.getY() > yMin && house.getY() < yMax)
                .toList();

        return new ResponseEntity<>(updateResponse, HttpStatus.OK);
    }

    private List<Map<String, String>> parsingGptResponse(String str) {
        List<Map<String, String>> results = new ArrayList<>();
        String[] sentences = str.split("\\r?\\n");
        for (String sentence : sentences) {
            HashMap<String, String> newMap = new HashMap<>();
            String exceptNumberStr = sentence.substring(sentence.indexOf(' ')).trim();
            String[] conditions = exceptNumberStr.split(",");
            for (String condition : conditions) {
                newMap.put(condition.split("-")[0].trim(), condition.split("-")[1].trim());
            }
            results.add(newMap);
        }

        return results;
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



    private String getKeywordANDWeightsFromGPT(String userInput) {
        String keywords = keyword();
        String command = createGPTCommand(userInput, keywords, publicData);

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

        return parseGPTResponse(result);
    }

    private String createGPTCommand(String userInput, String keywords, String publicData) {
        return String.format(
                "유저 입력 문장: '%s'. 보유 시설 데이터: '%s'. 보유 공공 데이터: '%s'. " +
                        "유저의 요구사항을 분석하여 반환 형식에 맞게 응답해주세요. 반환 형식은 세 가지 섹션으로 구성됩니다. " +
                        "각 섹션은 1,2,3 숫자로 구분되고 콤마로 구분된 키-값 쌍을 포함하며, 키와 값은 하이픈(-)으로 연결됩니다. 예를들어 피시방-0.1 이렇게 나타냅니다 " +
                        "반환양식의 1번은 매물 자체에 대한 추가 조건 (관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션) 을 의미하고 방 수-2, 관리비-10이하 와 같이 나타냅니다"+
                        "반환양식의 2번은 유저의 입력문장과 관련한 보유 시설 데이터와 가중치를 나타내고 음식점-0.2 와 같이 나타냅니다."+
                        "반환양식의 3번은 유저의 입력문장과 관련한 공공 데이터와 가중치를 나타내고 범죄율-0.2 와 같이 나타냅니다."+
                        "가중치는 유저의 요구사항에 따라 해당 데이터의 중요도를 나타내며, 2번과 3번 데이터의 가중치의 총합은 1이어야 합니다. " +
                        "불필요한 텍스트 없이 형식에 맞게 정확히 응답해주세요. 반환 형식 예시는 다음과 같습니다: " +
                        "'1. 관리비-20이하, 층수-3층, 복층-없음 2. 음식점-0.2, 피시방-0.2, 미용실-0.2, 병원-0.1 " +
                        "3. 교통사고율-0.1, 화재율-0.1, 범죄율-0.1'.",
                userInput, keywords, publicData
        );
    }





    private String parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");


        return content;
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
    }}

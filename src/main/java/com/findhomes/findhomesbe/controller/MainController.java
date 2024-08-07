package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Restaurant;
import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.repository.UserChatRepository;
import com.findhomes.findhomesbe.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final UserChatRepository userChatRepository;
    private final ChatGPTService chatGPTService;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;
    private final RestaurantIndustryService restaurantIndustryService;
    private final ChatService chatService;

    private List<House> preHouseData = new ArrayList<>();
    private String userInput = "방이 3개이고 화장실 수가 두개였으면 좋겠어. 버거킹이 가깝고, 역세권인 집 찾아줘. 또 나는 중학생인 딸을 키우고 있어. 지역이 학구열이 있었으면 좋겠어";
    private String publicData = "교통사고율,화재율,범죄율,생활안전,자살율,감염병율";
    @PostMapping("/api/search/man-con")
    @Operation(summary = "필수 조건 입력", description = "필수 조건을 입력하는 api입니다." +
            "\n\nhousingTypes 도메인: \"아파트\", \"원룸\", \"투룸\", \"쓰리룸\", \"쓰리룸 이상\", \"오피스텔\"")
    @ApiResponse(responseCode = "200", description = "챗봇 화면으로 이동해도 좋음.")
    public ResponseEntity<HashMap> setManConSearch(@RequestBody ManConRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(); // 헤더에 있는 세션 id로 세션이 있으면 찾고, 세션이 없으면 새로 생성

        /**
         * [매물 데이터 가져오기]
         * 필수 조건(계약 형태 및 가격, 월세, 집 형태)으로 필터링
         */
        // 필수 조건을 만족하는 매물 리스트 불러오기
        List<House> manConHouses = houseService.getManConHouses(request);
        log.info("매물 개수: {}개", manConHouses.size());

        // 세션에 필터링된 매물 리스트 저장
        session.setAttribute("preHouseData", manConHouses);
        // 세션 id 반환
        HashMap hashMap = new HashMap<>();
        hashMap.put("JSESSIONID", session.getId());
        return new ResponseEntity<>(hashMap, HttpStatus.OK);
    }

    @PostMapping("/api/search/user-chat")
    @Operation(summary = "사용자 채팅", description = "사용자 입력을 받고, 챗봇의 응답을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "챗봇 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserChatResponse.class))})
    public ResponseEntity<UserChatResponse> userChat(@RequestBody UserChatRequest userChatRequest, HttpServletRequest httpRequest) {
        // 요청 헤더에서 세션 ID를 추출 (클라이언트의 세션 ID)
        String sessionId = null;
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        // 세션 ID가 없으면 UNAUTHORIZED 응답 반환
        if (sessionId == null) {
            System.out.println("세션 id 없음");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // HttpServletRequest 객체에서 세션을 가져옴 (서버의 세션 객체)
        HttpSession session = httpRequest.getSession(false); // 세션이 없으면 null 반환

        // 서버의 세션 객체가 null이거나, 서버의 세션 ID와 클라이언트가 보낸 세션 ID가 일치하지 않으면 UNAUTHORIZED 응답 반환 (만료될 수 있음)
        if (session == null || !session.getId().equals(sessionId)) {
            System.out.println("서버에서의 세션 id 없음");

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatRepository.findBySessionId(sessionId);
        StringBuilder conversation = new StringBuilder();
        for (UserChat chat : previousChats) {
            conversation.append("User: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("Bot: ").append(chat.getGptResponse()).append("\n");
            }
        }

        // 사용자 입력 추가
        conversation.append("User: ").append(userChatRequest.getUserInput()).append("\n");

        // GPT에게 요청 보내기 (여기서 gptService를 사용하여 GPT 응답을 가져옵니다)
        String gptResponse = chatService.getResponse(conversation.toString());
        System.out.println(gptResponse);

        // 사용자 입력과 GPT 응답 저장
        UserChat userChat = new UserChat();
        userChat.setSessionId(sessionId);
        userChat.setUserInput(userChatRequest.getUserInput());
        userChat.setGptResponse(gptResponse);
        userChat.setCreatedAt(LocalDateTime.now());
        userChatRepository.save(userChat);

        // 응답 반환
        UserChatResponse response = new UserChatResponse();
        response.setChatResponse(gptResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/search/complete")
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.")
    @ApiResponse(responseCode = "200", description = "매물 응답 완료")
    public ResponseEntity<SearchResponse> getHouseList(HttpServletRequest httpRequest) {
        // 요청 헤더에서 세션 ID를 추출 (클라이언트의 세션 ID)
        String sessionId = null;
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        // 세션 ID가 없으면 UNAUTHORIZED 응답 반환
        if (sessionId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // HttpServletRequest 객체에서 세션을 가져옴 (세션이 없으면 null 반환)
        HttpSession session = httpRequest.getSession(false);

        // 서버의 세션 객체가 null이거나, 서버의 세션 ID와 클라이언트가 보낸 세션 ID가 일치하지 않으면 UNAUTHORIZED 응답 반환
        if (session == null || !session.getId().equals(sessionId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatRepository.findBySessionId(sessionId);
        StringBuilder conversation = new StringBuilder();
        for (UserChat chat : previousChats) {
            conversation.append("User: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("ChatBot: ").append(chat.getGptResponse()).append("\n");
            }
        }


        /**
         * [1. 키워드 및 가중치 선정]
         * GPT가 알려줘야 하는 데이터
         * 1) 필수 조건 외에 매물 자체에 대한 추가 조건 (관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션)
         * 2) 필요 시설 (ex. 버거킹, 정형외과, 다이소)
         * 3) 필요 공공 데이터와 가중치
         * 4) 사용자에게 추가로 물어봐야 할 조건?
         */
        String weights = getKeywordANDWeightsFromGPT(conversation.toString());
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
         * 3-2. 거리 기준으로 매물 필터링 + 점수 계산하기
         * 3-3. 공공데이터로 점수 계산하기
         */
        // 3-1. 필요 시설 가져오기
        List<Restaurant> restaurants = restaurantIndustryService.getRestaurantByKeyword(new String[]{"버거킹"});
        log.info("식당 개수: {}개", restaurants.size());
        // 3-2. 거리 기준으로 매물 필터링 + 점수 계산하기
        List<House> resultHouses = CoordService.filterHouseByDistance(this.preHouseData, restaurants, 5d);
        log.info("시설 필터링 후 매물 개수: {}개", resultHouses.size());
        // 3-3. 공공데이터로 점수 계산하기


        /**
         * [4. 점수로 매물 정렬 및 반환]
         */
        // 정렬
        //this.preHouseData.sort(Comparator.comparingDouble(House::getScore).reversed());
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



    private String getKeywordANDWeightsFromGPT(String converstation) {
        String keywords = keyword();
        String command = createGPTCommand(converstation, keywords, publicData);

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
                        "각 섹션은 '/'로 구분되고 콤마로 구분된 키-값 쌍을 포함하며, 키와 값은 하이픈(-)으로 연결됩니다. 예를들어 피시방-0.1 이렇게 나타냅니다 " +
                        "반환양식의 1번은 매물 자체에 대한 추가 조건 (관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션) 을 의미하고 방 수-2, 관리비-10이하 와 같이 나타냅니다"+
                        "반환양식의 2번은 유저의 입력문장과 관련한 보유 시설 데이터와 가중치를 나타내고 음식점-0.2 와 같이 나타냅니다."+
                        "반환양식의 3번은 유저의 입력문장과 관련한 공공 데이터와 가중치를 나타내고 범죄율-0.2 와 같이 나타냅니다."+
                        "가중치는 유저의 요구사항에 따라 해당 데이터의 중요도를 나타내며, 2번과 3번 데이터의 가중치의 총합은 1이어야 합니다. " +
                        "불필요한 텍스트 없이 형식에 맞게 정확히 응답해주세요. 반환 형식 예시는 다음과 같습니다: " +
                        "'관리비-20이하, 층수-3층, 복층-없음/음식점-0.2, 피시방-0.2, 미용실-0.2, 병원-0.1" +
                        "/교통사고율-0.1, 화재율-0.1, 범죄율-0.1'.",
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

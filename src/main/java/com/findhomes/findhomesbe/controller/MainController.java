package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.calculate.CalculateService;
import com.findhomes.findhomesbe.calculate.CoordService;
import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.calculate.data.SafetyEnum;
import com.findhomes.findhomesbe.calculate.SafetyGradeService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
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
    public static long millisecond = 0;

    public static long logMillisecond(String str) {
        if (millisecond == 0) {
            millisecond = System.currentTimeMillis();
            return 0;
        } else {
            long temp = System.currentTimeMillis() - millisecond;
            log.info("※ {} 걸린 시간: {}ms", str, temp);
            millisecond = System.currentTimeMillis();
            return temp;
        }
    }

    public static final double RADIUS = 5d;
    private final UserChatRepository userChatRepository;
    private final ChatGPTServiceImpl chatGPTServiceImpl;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;
    private final RestaurantIndustryService restaurantIndustryService;
    private final SafetyGradeService safetyGradeService;
    private final ChatService chatService;
    private final UserChatService userChatService;

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
        // TODO: 나중에 지워야 됨.
        this.preHouseData = manConHouses;
        //
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
        // 세션 ID 추출 및 검증
        String sessionId = extractSessionId(httpRequest);
        if (sessionId == null || !isValidSession(httpRequest, sessionId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(sessionId);
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
        userChatService.saveUserChat(sessionId, userChatRequest.getUserInput(), gptResponse);


        // 응답 반환
        UserChatResponse response = new UserChatResponse();
        response.setChatResponse(gptResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/search/complete")
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.")
    @ApiResponse(responseCode = "200", description = "매물 응답 완료")
    public ResponseEntity<SearchResponse> getHouseList(HttpServletRequest httpRequest) {
        // 세션 ID 추출 및 검증
        String sessionId = extractSessionId(httpRequest);
        if (sessionId == null || !isValidSession(httpRequest, sessionId)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(sessionId);
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
        logMillisecond("");
        String weights = getKeywordANDWeightsFromGPT(conversation.toString());
        log.info("GPT 응답: {}", weights);
        List<Map<String, String>> parsingResult = parsingGptResponse(weights, "/");
        for (Map<String, String> stringStringMap : parsingResult) {
            log.info("조건 파싱: {}", stringStringMap);
        }
        logMillisecond("1. GPT 응답 파싱");
        /**
         * [2. 매물 자체 조건으로 매물 필터링]
         * 데이터: 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션
         */
        this.preHouseData = houseService.filterByUserInput(parsingResult.get(0), preHouseData);
        log.info("사용자 입력 조건으로 필터링 후 매물 개수: {}개", preHouseData.size());
        logMillisecond("2. 사용자 입력 조건으로 매물 필터링");
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
        logMillisecond("3-1. 필요 시설 가져오기");
        // 3-2. 거리 기준으로 매물 필터링
        List<HouseWithCondition> resultHouseWithConditions = CoordService.filterAndCalculateByFacility(this.preHouseData, facilitiesMap, RADIUS);
        long temp = logMillisecond("3-2. 거리 기준으로 매물 필터링");
        log.info("※ 좌표 변환에 들어간 시간: {}ns", CoordService.accumulateTransform);
        log.info("※ 거리 계산에 들어간 시간: {}ns", CoordService.accumulateCalculateDistance);
        log.info("※ 3-2에서 좌표 변환의 비중: {}", CoordService.accumulateTransform / (1000 * temp));

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
        logMillisecond("3-3. 매물에 공공 데이터 정보 넣기");

        /**
         * [4. 점수로 매물 정렬 및 반환]
         * 4-1. 점수 계산 및 정렬
         */
        // 4-1. 점수 계산 및 정렬
        List<House> resultHouses = CalculateService.calculateScore(resultHouseWithConditions, parsingFacilityMap, parsingSafetyMap);
        this.preHouseData = resultHouses;
        logMillisecond("4-1. 점수 계산 및 정렬");
        // 응답 생성 및 반환
        SearchResponse.SearchResult searchResult = houseService.makeResponse(resultHouses.subList(0, Math.min(100, resultHouses.size())));
        SearchResponse searchResponse = new SearchResponse(true, 200, 200, "성공", searchResult);
        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
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

    private List<Map<String, String>> parsingGptResponse(String str, String splitRegex) {
        List<Map<String, String>> results = new ArrayList<>();
        String[] sentences = str.split(splitRegex);
        for (String sentence : sentences) {
            HashMap<String, String> newMap = new HashMap<>();
            String trimmedSentence = sentence.trim();
            String[] conditions = trimmedSentence.split(",");
            for (String condition : conditions) {
                newMap.put(condition.split("-")[0].trim(), condition.split("-")[1].trim());
            }
            results.add(newMap);
        }

        return results;
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
                .build();

        Map<String, Object> result = chatGPTServiceImpl.prompt(completionRequestDto);

        return parseGPTResponse(result);
    }

//    private String createGPTCommand(String userInput, String keywords, String publicData) {
//        return String.format(
//                "유저 입력 문장: '%s'. 보유 시설 데이터: '%s'. 보유 공공 데이터: '%s'. " +
//                "유저의 요구사항을 분석하여 반환 형식에 맞게 응답해주세요. 반환 형식은 세 가지 섹션으로 구성됩니다. " +
//                "각 섹션은 '/'로 구분되고 콤마로 구분된 키-값 쌍을 포함하며, 키와 값은 하이픈(-)으로 연결됩니다. 예를 들어 층수-3층/피시방-0.1/범죄율-0.9 이렇게 나타냅니다 " +
//                "반환 양식의 1번은 매물 자체에 대한 추가 조건 (관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션) 을 의미하고 방 수-2, 관리비-10이하 와 같이 나타냅니다"+
//                "반환 양식의 2번은 유저의 입력문장과 관련한 보유 시설 데이터와 가중치를 나타내고 음식점-0.2 와 같이 나타냅니다."+
//                "반환 양식의 3번은 유저의 입력문장과 관련한 공공 데이터와 가중치를 나타내고 범죄율-0.2 와 같이 나타냅니다."+
//                "예를 들어 '버거킹과 가까운 집 찾아줘'라는 조건이 들어왔을 때는 버거킹은 음식점에 포함되므로 음식점_버거킹-0.2로 포함관계를 나타내면서 키워드와 가중치를 반환합니다."+
//                "이 때 키워드의 포함관계는 _로 표시하고 이 포함관계를 나타내는 것은 반환양식의 2번에만 적용해줘"+
//                "가중치는 유저의 요구사항에 따른 해당 데이터의 중요도를 나타내며, 반환양식의 2번의 데이터들과 3번 데이터들을 합친 전체 데이터의 가중치의 총합은 1이어야 합니다.  " +
//                "불필요한 텍스트 없이 형식에 맞게 정확히 응답해주세요. 반환 형식 예시는 다음과 같습니다: " +
//                "'관리비-20이하, 층수-3층, 복층-없음/음식점_버거킹-0.2, 피시방-0.2, 미용실-0.2, 병원-0.1" +
//                "/교통사고율-0.1, 화재율-0.1, 범죄율-0.1'.",
//                userInput, keywords, publicData
//        );
//    }
    private String createGPTCommand(String userInput, String keywords, String publicData) {
        return String.format(
                "사용자 입력: '%s'. 사용 가능한 시설 데이터: '%s'. 사용 가능한 공공 데이터: '%s'. " +
                "사용자의 요구 사항을 분석하여 아래 형식에 따라 응답해주세요. 응답은 세 가지 섹션으로 구성됩니다. " +
                "각 섹션은 '/'로 구분되며, 키-값 쌍은 콤마로 구분되고, 각 키와 값은 하이픈('-')으로 연결됩니다. " +
                "\n\n응답 형식에는 4가지 섹션이 있고 각 섹션은 다음과 같습니다:\n" +
                "1. **매물 조건**: 매물 자체와 관련된 추가 조건을 명시합니다. 이 조건들은 매물의 특성에 대한 사용자 요구를 나타냅니다. " +
                "예를 들어 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션 등이 포함될 수 있습니다. " +
                "예시: '방 수-3, 화장실 수-2, 관리비-20 이하, 복층-없음'.\n\n" +
                "2. **시설 데이터**: 사용자 요구와 관련된 필요한 시설 데이터를 명시하고, 각 시설의 중요도를 가중치로 표시합니다. " +
                "이 가중치는 해당 시설이 사용자 요구와 얼마나 관련이 있는지를 나타냅니다. 가중치는 1부터 10사이의 값입니다." +
                "예를 들어 '음식점_버거킹-0.3'는 사용자가 버거킹과 가까운 집을 원할 경우, 음식점 중에서 버거킹의 중요도가 0.3임을 의미합니다. " +
                "'음식점_버거킹'과 같은 형식에서 '음식점'은 큰 범주를, '버거킹'은 그 범주 안의 특정 키워드를 의미합니다. " +
                "이처럼 포함관계의 속하는 데이터는 '_'로 표시합니다"+
                "예시: '음식점_버거킹-3, 피시방-2, 미용실-1, 병원-4'.\n\n" +
                "3. **공공 데이터**: 공공 데이터와 관련된 항목을 명시하고, 각 항목의 중요도를 가중치로 표시합니다. " +
                "이 가중치는 해당 공공 데이터가 사용자 요구와 얼마나 관련이 있는지를 나타냅니다. 예를 들어 '범죄율-3'는 " +
                "사용자가 안전한 지역을 원할 경우, 범죄율이 7의 중요도를 가지는 것을 의미합니다. " +
                "예시: '교통사고율-2, 화재율-1, 범죄율-7, 생활안전-2, 자살율-3'.\n\n" +
                "4. 특정 좌표: 사용자가 가까웠으면 하는 특정 지점의 좌표입니다. "+
                "예를 들어 '네이버 본사와 강남역이랑 가까웠으면 좋겠다'와 같은 조건을 받으면 네이버본사_(37.359512,127.105220)-2, 강남역_(37.497940,127.027620)-2 와 같이 나타냅니다."+
                "모든 섹션의 형식을 정확히 준수하여, 불필요한 텍스트 없이 응답해주세요. 반환 형식 예시는 다음과 같습니다."+
                "'관리비-20이하, 층수-3층, 복층-없음/음식점_버거킹-5, 피시방-2, 미용실-1, 병원-3" +
                "/교통사고율-3, 화재율-1, 범죄율-4/강남역_(37.497940,127.027620)-3'."
                , userInput, keywords, publicData
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
    }
    // 클라이언트 헤더의 쿠키에서 세션 ID 추출
    private String extractSessionId(HttpServletRequest httpRequest) {
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    // 해당 세션이 유효한지 검사
    private boolean isValidSession(HttpServletRequest httpRequest, String sessionId) {
        HttpSession session = httpRequest.getSession(false);
        return session != null && sessionId.equals(session.getId());
    }
}

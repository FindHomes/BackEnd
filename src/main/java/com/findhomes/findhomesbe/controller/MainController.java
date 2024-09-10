package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.condition.service.ConditionService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.login.JwtTokenProvider;
import com.findhomes.findhomesbe.repository.UserChatRepository;
import com.findhomes.findhomesbe.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class MainController {

    public static final double RADIUS = 5d;
    public static final String MAN_CON_KEY = "man-con";
    public static final String HOUSE_RESULTS_KEY = "house-result";

    private final UserChatRepository userChatRepository;
    private final ChatGPTServiceImpl chatGPTServiceImpl;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;
    private final RestaurantIndustryService restaurantIndustryService;
    private final ChatService chatService;
    private final UserChatService userChatService;
    private final ConditionService conditionService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/api/search/man-con")
    @Operation(summary = "필수 조건 입력", description = "필수 조건을 입력하는 api입니다." +
            "\n\nhousingTypes 도메인: \"아파트\", \"원룸\", \"투룸\", \"쓰리룸\", \"쓰리룸 이상\", \"오피스텔\"\n\n" +
            "응답: 추천 문장 3개를 반환합니다. (주의: gpt가 형식에 맞지 않게 응답을 반환하면 문장이 더 많거나 적을 수 있습니다.)")
    @ApiResponse(responseCode = "200", description = "챗봇 화면으로 이동해도 좋음.")
    public ResponseEntity<ManConResponse> setManConSearch(@RequestBody ManConRequest request, HttpServletRequest httpRequest) {
        // 토큰 검사
        String token = extractTokenFromRequest(httpRequest);
        jwtTokenProvider.validateToken(token);

        sessionCheck(request,httpRequest);

        // 추천 질문 생성
        String gptOutput = getGptOutput(makeUserRecommendInput(request));

        // 응답 반환
        ManConResponse response = new ManConResponse(true, 200, "필수 조건이 잘 저장되었습니다.", Arrays.stream(gptOutput.split("\n")).map(str -> str.trim()).collect(Collectors.toList()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    private String makeUserRecommendInput(ManConRequest request) {
        // TODO: 여기에 유저 정보도 넣어 줘야 할 듯.
        return "[유저가 입력한 필수 조건]\n" + request.toSentence() + "\n" +
                "[보유 데이터 목록]\n" + conditionService.conditionsToSentence() + "\n" +
                "위의 데이터를 참고해서 유저에게 매물을 찾을 때 입력할 조건을 추천해줘.\n" +
                "이 서비스는 보유 데이터 목록을 기반으로 유저가 입력한 조건에 맞는 부동산 매물을 찾아주는 서비스야.\n" +
                "보유한 데이터를 창의적으로 활용할 수 있는 조건도 상관없어. 보유한 데이터 내에서만 질문할 필요도 없어. 유저는 입력을 자유롭게 할 수 있어.\n" +
                "예시: 주변에 쇼핑할 곳이 있으면 좋을 것 같아. 그리고 아이 키우기 좋은 곳으로 추천해줘.\n" +
                "문장의 길이는 100자 안으로 해주고, 3개의 문장을 랜덤하게 추천해줘. 조건 여러개를 하나의 문장에 써도 좋아.\n" +
                "각 문장은 \\n으로 구분해서 한 줄에 하나의 문장만 나오게 해줘. 그 외에 다른 말은 아무것도 붙이지 말아줘.";
    }


    @PostMapping("/api/search/user-chat")
    @Operation(summary = "사용자 채팅", description = "사용자 입력을 받고, 챗봇의 응답을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "챗봇 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserChatResponse.class))})
    @ApiResponse(responseCode = "204", description = "챗봇 대화 종료", content = {@Content(mediaType = "application/json")}
    )
    public ResponseEntity<UserChatResponse> userChat(@RequestBody UserChatRequest userChatRequest, HttpServletRequest httpRequest) {

        String token = extractTokenFromRequest(httpRequest);
        jwtTokenProvider.validateToken(token);
        // 세션 ID 가져오기
        HttpSession session = httpRequest.getSession(false); // 기존 세션을 가져옴
        if (session == null) {
            System.out.println("세션 없음");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 세션이 없으면 에러 반환
        }
        String chatSessionId = session.getId();

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(chatSessionId);
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
        userChatService.saveUserChat(chatSessionId, userChatRequest.getUserInput(), gptResponse);
        // 대화 종료 조건 확인
        if (gptResponse.contains("대화 종료")) {
            // 대화 종료를 클라이언트에 알리기
            return ResponseEntity.noContent().build();
        }

        // 응답 반환
        UserChatResponse response = new UserChatResponse(true, 200, "성공", new UserChatResponse.ChatResponse(gptResponse));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/search/complete")
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.\n\n" +
            "최대 100개의 매물을 점수를 기준으로 내림차순으로 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "401", description = "session이 없습니다. 필수 조건 입력 창으로 돌아가야 합니다.")
    })
    public ResponseEntity<SearchResponse> getHouseList(
            HttpServletRequest httpRequest,
            @SessionAttribute(value = MAN_CON_KEY, required = false) ManConRequest manConRequest
    ) {
        String token = extractTokenFromRequest(httpRequest);
        jwtTokenProvider.validateToken(token);
        // 세션 ID 가져오기
        HttpSession session = httpRequest.getSession(false); // 기존 세션을 가져옴
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 세션이 없으면 에러 반환
        }
        String chatSessionId = session.getId();

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(chatSessionId);
        StringBuilder conversation = new StringBuilder();
        for (UserChat chat : previousChats) {
            conversation.append("User: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("ChatBot: ").append(chat.getGptResponse()).append("\n");
            }
        }

        // GPT 응답 반환
        String gptResponse = getGptOutput(createGPTCommand(conversation.toString()));
        log.info("\n<GPT 응답>\n{}", gptResponse);
        // 매물 점수 계산해서 가져오기
        List<House> resultHouses = conditionService.exec(manConRequest, gptResponse);

        // 결과 반환
        if (resultHouses.isEmpty()) {
            return new ResponseEntity<>(new SearchResponse(new ArrayList<>(), true, 200, "No Content"), HttpStatus.OK);
        } else {
            List<House> subResultHouses = resultHouses.subList(0, Math.min(100, resultHouses.size()));

            // log 출력 for문
            for (House house : subResultHouses) {
                log.info("최종 결과 - 매물id: {} / 총 점수: {} / 공공 데이터 점수: {} / 시설 데이터 점수: {}", house.getHouseId(), house.getScore(), house.getPublicDataScore(), house.getFacilityDataScore());
            }

            return new ResponseEntity<>(new SearchResponse(subResultHouses, true, 200, "성공"), HttpStatus.OK);
        }
    }

    private String getGptOutput(String command) {
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


//    private String createGPTCommand(String userInput) {
//        return String.format(
//                "사용자의 조건을 입력받고 그 조건과 관련된 데이터를 찾아 데이터를 활용하여 조건에 맞는 부동산을 추천해주는 앱을 만들고 있습니다." +
//                "사용자 조건 입력: '%s'. 매물 옵션: '%s'. 사용 가능한 시설 데이터: '%s'. 사용 가능한 공공 데이터: '%s'. " +
//                "사용자의 요구 사항을 분석하여 아래 형식에 따라 응답해주세요. 응답은 5가지 섹션으로 구성됩니다. " +
//                "각 섹션은 한 줄에 하나씩 표시됩니다. 전체 응답은 총 5줄로 이루어져있어야 합니다. 개행 문자는 섹션들 사이를 구분하는 기준으로만 사용되며, 각 섹션 사이에는 반드시 하나의 개행 문자만 있어야 합니다." +
//                "또한 어떤 섹션에 내용이 없다고 하더라도 그 줄이 생략되어서는 안되고, 빈 줄이더라도 있어야 합니다. 각 섹션은 그 섹션에 해당하는 줄에 위치하고, 전체 응답은 항상 총 5줄입니다.(즉, 개행 문자는 4개만 있어야 합니다.)" +
//                "\n\n응답 형식에는 5가지 섹션이 있으며 각 섹션은 다음과 같습니다:\n" +
//                "섹션1(반드시 응답의 첫째줄에 위치 해야 함). **매물 조건**: 매물 자체와 관련된 추가 조건을 명시합니다. 이 조건들은 매물의 특성에 대한 사용자 요구를 나타냅니다. " +
//                "매물 조건에는 '%s'이 포함될 수 있습니다. " +
//                "예시: '관리비-10, 복층-false, 분리형-true, 층수-3, 크기-30, 방_수-3, 화장실_수-2, 방향-동, 완공일-20241023' 와 같은 양식으로 나타냅니다. 복층과 분리형은 true나 false로 표현하고 방향은 '%s'으로, 완공일은 날짜로, 크기는 제곱미터 단위로 표현합니다.(평수일 경우 제곱미터로 변환)\n\n" +
//                "섹션2(반드시 응답의 둘째줄에 위치 해야 함). 매물 옵션 : 매물에 관한 옵션 중 사용자에게 필요한 옵션을 명시합니다. 예시: 화재경보기,신발장,옷장.\n\n" +
//                "섹션3(반드시 응답의 셋째줄에 위치 해야 함). **시설 데이터**: 사용자 요구와 관련된 필요한 시설 데이터를 명시하고, 각 시설의 중요도를 가중치로 표시합니다. " +
//                "이 가중치는 해당 시설이 사용자 요구와 얼마나 관련이 있는지를 나타냅니다. 가중치는 1부터 10 사이의 값입니다." +
//                "예를 들어 '음식점_버거킹-0.3'는 사용자가 버거킹과 가까운 집을 원할 경우, 음식점 중에서 버거킹의 중요도가 0.3임을 의미합니다. " +
//                "'음식점_버거킹'과 같은 형식에서 '음식점'은 큰 범주를, '버거킹'은 그 범주 안의 특정 키워드를 의미합니다. " +
//                "이처럼 포함관계에 속하는 데이터는 '_'로 표시합니다. 특정 카테고리 전부일 경우 특정 이름이 아니라 all로 표시합니다." +
//                "예시: '음식점_버거킹-3, 피시방_all-2, 미용실_all-1, 병원_이비인후과-4, 병원_소아과-3'.\n\n" +
//                "섹션4(반드시 응답의 넷째줄에 위치 해야 함). **공공 데이터**: 공공 데이터와 관련된 항목을 명시하고, 각 항목의 중요도를 가중치로 표시합니다. " +
//                "이 가중치는 해당 공공 데이터가 사용자 요구와 얼마나 관련이 있는지를 나타냅니다. 예를 들어 '범죄율-3'는 " +
//                "사용자가 안전한 지역을 원할 경우, 범죄율이 7의 중요도를 가지는 것을 의미합니다. " +
//                "예시: '교통사고율-2, 화재율-1, 범죄율-7, 생활안전-2, 자살율-3'.\n\n" +
//                "섹션5(반드시 응답의 다섯째줄에 위치 해야 함). **특정 좌표**: 사용자가 가까웠으면 하는 특정 지점의 좌표입니다. 위도와 경도는 '+'로 구분되고 (37+127)과 같이 표현됩니다. 다음과 같은 양식을 지켜야합니다." +
//                "예를 들어 '네이버 본사와 강남역이랑 가까웠으면 좋겠다'와 같은 조건을 받으면 네이버본사_(37.359512+127.105220)-2, 강남역_(37.497940+127.027620)-2 와 같이 나타냅니다." +
//                "\n모든 섹션의 형식을 정확히 준수하여, 불필요한 텍스트 없이 응답해주세요. 반환 형식 예시는 다음과 같습니다." +
//                "'관리비-20, 층수-3, 복층-true\n가스레인지,샤워부스\n음식점_버거킹-5, 피시방_all-2, 미용실_all-1, 병원_이비인후과-3\n교통사고율-3, 화재율-1, 범죄율-4\n강남역_(37.497940+127.027620)-3'." +
//                "응답은 항상 한글이어야 합니다. '\\n'는 섹션들 사이에서 구분하는 기준으로만 쓰여야 하고, 이 문자는 항상 반드시 총 4개여야 합니다. 만약에 없는 섹션이 없다면 개헹으로 비워둬야합니다.",
//                userInput, HouseOption.getAllData(), FacilityCategory.getAllData(), PublicData.getAllData(), HouseCondition.getAllData(), HouseDirection.getAllData()
//        );
//    }

    private String createGPTCommand(String userInput) {
        return String.format(
                "부동산 추천 앱을 위해 사용자의 조건을 입력받고, 그 조건에 맞는 매물과 관련된 데이터를 찾아 최적의 부동산을 추천하고 있습니다. " +
                        "사용자는 다음과 같은 조건을 입력했습니다: '%s'. 이 조건을 기반으로 하여, 아래의 각 항목에 맞춰 응답을 구성해주세요. " +
                        "응답은 정확히 5줄로 구성되어야 하며, 각 줄은 하나의 항목을 나타냅니다. 각 항목은 반드시 해당 줄에 위치해야 하며, 항목들 사이에는 반드시 하나의 개행 문자('\\n')만 있어야 합니다. " +
                        "응답의 형식은 정확히 지켜져야 하며, 불필요한 공백이나 추가 문자가 포함되지 않아야 합니다." +
                        "\n\n응답 형식은 다음과 같은 5개의 항목으로 구성됩니다:\n" +

                        "1. **매물 조건** (반드시 첫 번째 줄에 위치): 매물의 특성에 대한 사용자의 추가 요구사항을 명시합니다. " +
                        "사용자가 입력한 조건과 직접적으로 관련이 있는 경우에만 응답하며, 다음 중 하나 이상의 요소가 포함될 수 있습니다: '%s'. 방향 관련 조건이 있다면 반환 양식은 다음 중 하나로 명시되어야 합니다: '%s' " +
                        "예시: '관리비-20, 복층-false, 분리형-true, 층수-2, 크기-30, 방_수-1, 화장실_수-1, 방향-남, 완공일-20241023'. " +
                        "사용자가 입력한 조건에 매물의 특성과 관련된 내용이 없다면 이 항목은 빈 줄로 남겨두세요.\n\n" +

                        "2. **매물 옵션** (반드시 두 번째 줄에 위치): 사용자가 원하는 매물에 포함되어야 하는 옵션을 나열합니다. " +
                        "사용자가 입력한 조건과 직접적으로 관련이 있는 옵션만 응답합니다. 사용 가능한 매물 옵션 목록: '%s'. " +
                        "예시: '화재경보기,신발장,옷장'. 사용자가 입력한 조건과 매물 옵션이 관련이 없다면 이 항목은 빈 줄로 남겨두세요.\n\n" +

                        "3. **시설 데이터** (반드시 세 번째 줄에 위치): 사용자가 원하는 시설과 해당 시설의 중요도를 가중치로 표시합니다. 가중치는 1에서 10 사이의 값으로, 시설이 사용자 요구에 얼마나 중요한지를 나타냅니다. " +
                        "사용 가능한 시설 데이터 목록: '%s'. 예시: '음식점_버거킹-8, 피시방_all-2, 미용실_all-1, 병원_이비인후과-4, 병원_소아과-3'. " +
                        "각 시설 데이터는 '시설_세부명칭-가중치'의 형식으로 작성되며, 'all'은 해당 범주 전체를 나타냅니다. 사용자가 요청한 시설과 관련된 데이터가 없을 경우 이 항목은 빈 줄로 남겨두세요.\n\n" +

                        "4. **공공 데이터** (반드시 네 번째 줄에 위치): 사용자 요구와 관련된 공공 데이터를 명시하고, 각 데이터의 중요도를 가중치로 표시합니다. " +
                        "가중치는 1에서 10 사이의 값으로, 공공 데이터의 중요도를 나타냅니다. 사용 가능한 공공 데이터 목록: '%s'. " +
                        "예시: '교통사고율-2, 화재율-1, 범죄율-9, 생활안전-5, 자살율-3'. 사용자가 요청한 공공 데이터와 관련된 항목이 없을 경우 이 항목은 빈 줄로 남겨두세요.\n\n" +

                        "5. **특정 좌표** (반드시 다섯 번째 줄에 위치): 사용자가 선호하는 특정 지점의 좌표를 표시합니다. 예를 들어 '네이버본사_(37.359512+127.105220)-2, 강남역_(37.497940+127.027620)-2'. " +
                        "각 좌표 데이터는 '지점명_(위도+경도)-가중치'의 형식으로 작성됩니다. 사용자가 입력한 조건과 관련된 특정 지점이 없을 경우 이 항목은 빈 줄로 남겨두세요." +
                        "\n\n응답은 반드시 한글로 작성되어야 하며, 각 항목은 정의된 형식을 준수해야 합니다. 특정 항목에 대한 정보가 없더라도 개행문자와 함께 빈 줄로 표시해야 합니다. 전체 응답은 반드시 5개의 줄로 구성되어야 하며, 4개의 개행 문자('\\n')가 포함되어 있어야 합니다." +
                        "\n모든 항목의 형식을 정확히 준수하여, 불필요한 텍스트 없이 응답해주세요. 반환 형식 예시는 다음과 같습니다: " +
                        "'관리비-20, 층수-3, 복층-true\n에어컨\n음식점_버거킹-5, 피시방_all-2, 미용실_all-1, 병원_이비인후과-3\n교통사고율-3, 화재율-1, 범죄율-4\n강남역_(37.497940+127.027620)-3'.",
                userInput, HouseCondition.getAllData(), HouseDirection.getAllData(), HouseOption.getAllData(), FacilityCategory.getAllData(), PublicData.getAllData()
        );
    }



    private String parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");
        return content;
    }


    // 클라이언트 요청 헤더에서 JWT 토큰 추출
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }
    // 기존 세션 무효화 및 새로운 세션 생성
    private void sessionCheck(ManConRequest request, HttpServletRequest httpRequest) {
        // 기존 세션 무효화
        HttpSession existingSession = httpRequest.getSession(false); // 기존 세션이 있을 경우 가져옴
        if (existingSession != null) {
            existingSession.invalidate(); // 기존 세션 무효화
        }
        // 새로운 세션 생성
        HttpSession session = httpRequest.getSession(true);
        String chatSessionId = session.getId();
        log.info("새로운 대화 세션 ID 생성: {}", chatSessionId);
        // 세션에 필터링된 필수 조건 저장
        log.info("입력된 필수 조건: {}", request);
        session.setAttribute(MAN_CON_KEY, request);
    }
}

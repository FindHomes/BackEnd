package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.argument_resolver.SessionValue;
import com.findhomes.findhomesbe.calculate.service.SafetyGradeService;
import com.findhomes.findhomesbe.condition.service.ConditionService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.repository.UserChatRepository;
import com.findhomes.findhomesbe.service.*;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MainController {

    public static final double RADIUS = 5d;
    public static final String MAN_CON_KEY = "man-con";

    private final UserChatRepository userChatRepository;
    private final ChatGPTServiceImpl chatGPTServiceImpl;
    private final KaKaoMapService kaKaoMapService;
    private final HouseService houseService;
    private final HospitalService hospitalService;
    private final RestaurantIndustryService restaurantIndustryService;
    private final SafetyGradeService safetyGradeService;
    private final ChatService chatService;
    private final UserChatService userChatService;
    private final ConditionService conditionService;

    private List<House> preHouseData = new ArrayList<>();
    private String userInput = "방이 3개이고 화장실 수가 두개였으면 좋겠어. 버거킹이 가깝고, 역세권인 집 찾아줘. 또 나는 중학생인 딸을 키우고 있어. 지역이 학구열이 있었으면 좋겠어";
    private String publicData = "교통사고율,화재율,범죄율,생활안전,자살율,감염병율";
    @PostMapping("/api/search/man-con")
    @Operation(summary = "필수 조건 입력", description = "필수 조건을 입력하는 api입니다." +
            "\n\nhousingTypes 도메인: \"아파트\", \"원룸\", \"투룸\", \"쓰리룸\", \"쓰리룸 이상\", \"오피스텔\"")
    @ApiResponse(responseCode = "200", description = "챗봇 화면으로 이동해도 좋음.")
    public ResponseEntity<ManConResponse> setManConSearch(@RequestBody ManConRequest request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(); // 헤더에 있는 세션 id로 세션이 있으면 찾고, 세션이 없으면 새로 생성
        // 세션에 필터링된 필수 조건 저장
        session.setAttribute(MAN_CON_KEY, request);

        // 응답 반환
        ManConResponse response = new ManConResponse(true, 200, "성공", new ManConResponse.JSESSIONID(session.getId()));
        return new ResponseEntity<>(response, HttpStatus.OK);
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
        String gptResponse = chatService.getResponse(conversation.toString(), keyword()+","+publicData);
        System.out.println(gptResponse);

        // 사용자 입력과 GPT 응답 저장
        userChatService.saveUserChat(sessionId, userChatRequest.getUserInput(), gptResponse);


        // 응답 반환
        UserChatResponse response = new UserChatResponse(true, 200, "성공", new UserChatResponse.ChatResponse(gptResponse));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/search/complete")
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.")
    @ApiResponse(responseCode = "200", description = "매물 응답 완료")
    public ResponseEntity<SearchResponse> getHouseList(
            HttpServletRequest httpRequest,
            @SessionValue(MAN_CON_KEY) ManConRequest manConRequest
    ) {
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

        // GPT 응답 반환
        String weights = getKeywordANDWeightsFromGPT(conversation.toString());
        log.info("GPT 응답: {}", weights);
        // 매물 점수 계산해서 가져오기
        return new ResponseEntity<>(conditionService.exec(manConRequest, weights), HttpStatus.OK);
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


    private String createGPTCommand(String userInput, String keywords, String publicData) {
        return String.format(
                "사용자 입력: '%s'. 사용 가능한 시설 데이터: '%s'. 사용 가능한 공공 데이터: '%s'. " +
                "사용자의 요구 사항을 분석하여 아래 형식에 따라 응답해주세요. 응답은 세 가지 섹션으로 구성됩니다. " +
                "각 섹션은 '/'로 구분되며, 키-값 쌍은 콤마로 구분되고, 각 키와 값은 하이픈('-')으로 연결됩니다. " +
                "\n\n응답 형식에는 4가지 섹션이 있고 각 섹션은 다음과 같습니다:\n" +
                "1. **매물 조건**: 매물 자체와 관련된 추가 조건을 명시합니다. 이 조건들은 매물의 특성에 대한 사용자 요구를 나타냅니다. " +
                "예를 들어 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션이 포함될 수 있습니다. " +
                "예시: '관리비-10, 복층-false, 분리형-true, 층수-3, 크기-30, 방 수-3, 화장실 수-2, 방향-동, 완공일-20241023' 와 같은 양식으로 나타냅니다. 복층과 분리형은 true나 false로 표현하고 방향은 동, 서,남,북으로, 완공일은 날짜, 나머지 옵션은 정수로 표현합니다.\n\n" +
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
                "4. 특정 좌표: 사용자가 가까웠으면 하는 특정 지점의 좌표입니다. 위도와 경도는 +로 구분되고 (37+127)과 같이 표현됩니다. 다음과 같은 양식을 지켜야합니다."+
                "예를 들어 '네이버 본사와 강남역이랑 가까웠으면 좋겠다'와 같은 조건을 받으면 네이버본사_(37.359512+127.105220)-2, 강남역_(37.497940+127.027620)-2 와 같이 나타냅니다."+
                "모든 섹션의 형식을 정확히 준수하여, 불필요한 텍스트 없이 응답해주세요. 반환 형식 예시는 다음과 같습니다."+
                "'관리비-20, 층수-3, 복층-true/음식점_버거킹-5, 피시방-2, 미용실-1, 병원-3" +
                "/교통사고율-3, 화재율-1, 범죄율-4/강남역_(37.497940+127.027620)-3'."
                , userInput, keywords, publicData
        );
    }

    private String parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");


        return content;
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

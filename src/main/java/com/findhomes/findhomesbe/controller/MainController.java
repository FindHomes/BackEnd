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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final SecurityService securityService;

    @PostMapping("/api/search/man-con")
    public ResponseEntity<ManConResponse> setManConSearch(@RequestBody ManConRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        // 토큰 검사
        String token = securityService.extractTokenFromRequest(httpRequest);
        jwtTokenProvider.validateToken(token);
        // 세션 검사
        securityService.sessionCheck(request, httpRequest);

        // 세션 ID 가져오기
        HttpSession session = httpRequest.getSession(false); // 기존 세션을 가져옴
        String chatSessionId = session.getId();

        // 쿠키에 세션 ID 추가
        Cookie sessionCookie = new Cookie("JSESSIONID", chatSessionId);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        // 추천 질문 생성
        String gptOutput = chatGPTServiceImpl.getGptOutput(makeUserRecommendInput(request));

        // 응답 반환
        ManConResponse responseBody = new ManConResponse(true, 200, "필수 조건이 잘 저장되었습니다.", Arrays.stream(gptOutput.split("\n")).map(str -> str.trim()).collect(Collectors.toList()));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
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
    public ResponseEntity<UserChatResponse> userChat(@RequestBody UserChatRequest userChatRequest, HttpServletRequest httpRequest, @SessionAttribute(value = MAN_CON_KEY, required = false) ManConRequest manConRequest) {
        // 토큰 검사
        String token = securityService.extractTokenFromRequest(httpRequest);
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
            conversation.append("사용자: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("챗봇: ").append(chat.getGptResponse()).append("\n");
            }
        }

        // 사용자 입력 추가
        conversation.append("User: ").append(userChatRequest.getUserInput()).append("\n");
        conversation.append("사전에 사용자 입력한 조건 :").append(manConRequest.toSentence());
        conversation.append("추가로 사용자가 사전에 입력한 조건을 고려해서 응답하고, **와 같은 마크다운 방식으로 응답하지말고 순수 string으로 응답해줘 ");


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
        // 토큰 검사
        String token = securityService.extractTokenFromRequest(httpRequest);
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

        // 전체 대화 내용을 기반으로 GPT 응답 반환 (조건 - 데이터 매칭)
        String gptResponse = chatGPTServiceImpl.getGptOutput(chatGPTServiceImpl.createGPTCommand(conversation.toString()));
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
}

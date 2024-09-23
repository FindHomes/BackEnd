package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.condition.service.ConditionService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.gpt.ChatGPTServiceImpl;
import com.findhomes.findhomesbe.login.JwtTokenProvider;
import com.findhomes.findhomesbe.repository.HouseRepository;
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

import static com.findhomes.findhomesbe.gpt.ChatGPTConst.*;
import static com.findhomes.findhomesbe.gpt.CommandService.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class MainController {

    public static final double RADIUS = 5d;
    public static final String MAN_CON_KEY = "man-con";
    public static final String HOUSE_RESULTS_KEY = "house-result";

    private final ChatGPTServiceImpl chatGPTServiceImpl;
    private final UserChatService userChatService;
    private final ConditionService conditionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityService securityService;
    private final HouseRepository houseRepository;
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
        String command = createUserConditionCommand(conditionService.conditionsToSentence());
        String gptOutput = chatGPTServiceImpl.getGptOutput(command, ROLE1, ROLE2, COMPLETE_CONTENT, 0.9);

        // 응답 반환
        ManConResponse responseBody = new ManConResponse(true, 200, "필수 조건이 잘 저장되었습니다.", Arrays.stream(gptOutput.split("\n")).map(str -> str.replaceAll("^가-힣", "").trim()).filter(str -> !str.isEmpty()).collect(Collectors.toList()));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
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
        conversation.append("[이전 대화 기록]");
        for (UserChat chat : previousChats) {
            conversation.append("사용자: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("챗봇: ").append(chat.getGptResponse()).append("\n");
            }
        }

        // 사용자 입력 추가 및 대화 응답 조정
        String command = createChatCommand(
                userChatRequest.getUserInput(),
                FacilityCategory.getAllData() + PublicData.getAllData()
        );
        conversation.append(command.replaceAll("^가-힣", ""));

        // GPT에게 요청 보내기 (여기서 gptService를 사용하여 GPT 응답을 가져옵니다)
        String gptResponse = chatGPTServiceImpl.getGptOutput(conversation.toString(), ROLE1, ROLE2, CHAT_CONTENT, 0.9);
        System.out.println(gptResponse);

        // 사용자 입력과 GPT 응답 저장
        userChatService.saveUserChat(chatSessionId, userChatRequest.getUserInput(), gptResponse);
        // 대화 종료 조건 확인
//        if (gptResponse.contains("대화 종료")) {
//            // 대화 종료를 클라이언트에 알리기
//            return ResponseEntity.noContent().build();
//        }

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
            conversation.append("사용자: ").append(chat.getUserInput()).append("\n");
            if (chat.getGptResponse() != null) {
                conversation.append("챗봇: ").append(chat.getGptResponse()).append("\n");
            }
        }
        log.info(createCompleteCommand(conversation.toString()));
        // 전체 대화 내용을 기반으로 GPT 응답 반환 (조건 - 데이터 매칭)
        String gptResponse = chatGPTServiceImpl.getGptOutput(createCompleteCommand(conversation.toString()), ROLE1, ROLE2, COMPLETE_CONTENT, 0.1);
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
    @GetMapping("/api/house/{houseId}")
    @Operation(summary = "매물 상세페이지", description = "매물을 클릭하고 상세페이지로 이동합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "401", description = "유효한 session이 없습니다. 필수 조건 입력 창으로 돌아가야 합니다."),
            @ApiResponse(responseCode = "404", description = "입력 id에 해당하는 매물이 없습니다")
    })
    public ResponseEntity<HouseDetailResponse> getHouseDetail(
            HttpServletRequest httpRequest, @PathVariable int houseId
    ) {
        // 토큰 검사
        String token = securityService.extractTokenFromRequest(httpRequest);
        jwtTokenProvider.validateToken(token);
        // 세션 ID 가져오기
        HttpSession session = httpRequest.getSession(false); // 기존 세션을 가져옴
        if (session == null) {
            return new ResponseEntity<>(new HouseDetailResponse(false, 401, "세션 없음", null), HttpStatus.UNAUTHORIZED);
        }

        // 매물 정보 조회
        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            return new ResponseEntity<>(new HouseDetailResponse(false, 404, "id에 해당하는 매물 없음", null), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new HouseDetailResponse(true, 200, "성공", house), HttpStatus.OK);
    }
}

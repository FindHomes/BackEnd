package com.findhomes.findhomesbe.domain.condition;

import com.findhomes.findhomesbe.domain.chat.UserChat;
import com.findhomes.findhomesbe.domain.chat.UserChatService;
import com.findhomes.findhomesbe.domain.chat.gpt.ChatGPTServiceImpl;
import com.findhomes.findhomesbe.domain.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.domain.condition.domain.SessionKeys;
import com.findhomes.findhomesbe.domain.condition.dto.ManConRequest;
import com.findhomes.findhomesbe.domain.condition.dto.ManConResponse;
import com.findhomes.findhomesbe.domain.condition.service.ConditionService;
import com.findhomes.findhomesbe.domain.house.domain.House;
import com.findhomes.findhomesbe.domain.house.dto.SearchResponse;
import com.findhomes.findhomesbe.global.Response;
import com.findhomes.findhomesbe.global.auth.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import static com.findhomes.findhomesbe.domain.chat.gpt.ChatGPTConst.*;
import static com.findhomes.findhomesbe.domain.chat.gpt.CommandService.createUserConditionCommand;
import static com.findhomes.findhomesbe.domain.chat.gpt.ChatGPTConst.COMPLETE_CONTENT;

@RestController
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ConditionController {

    private final ChatGPTServiceImpl chatGPTServiceImpl;
    private final UserChatService userChatService;
    private final ConditionService conditionService;
    private final SecurityService securityService;
    @PostMapping("/api/search/man-con")
    public ResponseEntity<ManConResponse> setManConSearch(@RequestBody ManConRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        HttpSession session = securityService.getNewSession(httpRequest);
        session.setAttribute(SessionKeys.MAN_CON_KEY, request);
        log.info("MANCON: {}", request.toSentence());

        String sessionId = session.getId();
        securityService.addSessionIdOnCookie(sessionId, response);

        // 추천 질문 생성
        String command = createUserConditionCommand(conditionService.conditionsToSentence());
        String gptOutput = chatGPTServiceImpl.getGptOutput(command, ROLE1, ROLE2, COMPLETE_CONTENT, USER_CONDITION_TEMPERATURE);

        // 응답 반환
        ManConResponse responseBody = new ManConResponse(true, 200, "필수 조건이 잘 저장되었습니다.", Arrays.stream(gptOutput.split("\n")).map(str -> str.replaceAll("[^가-힣0-9a-zA-Z .,]", "").trim()).filter(str -> !str.isEmpty()).collect(Collectors.toList()));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }


    @GetMapping("/api/search/complete")
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.\n\n" + "최대 100개의 매물을 점수를 기준으로 내림차순으로 반환합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "매물 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SearchResponse.class))}), @ApiResponse(responseCode = "401", description = "session이 없습니다. 필수 조건 입력 창으로 돌아가야 합니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))}), @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))})})
    public ResponseEntity<SearchResponse> getHouseList(HttpServletRequest httpRequest, @Parameter(hidden = true) @SessionAttribute(value = SessionKeys.MAN_CON_KEY, required = false) ManConRequest manConRequest) {
        System.out.println(manConRequest);
        HttpSession session = securityService.getSession(httpRequest);
        String chatSessionId = session.getId();

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(chatSessionId);
        StringBuilder conversation = userChatService.buildConversation(previousChats);
        // 대화에서 키워드 추출 하기
        String input = conversation.toString() + "\n" + EXTRACT_KEYWORD_COMMAND;
        String keywordStr = chatGPTServiceImpl.getGptOutput(input, ROLE1, ROLE2, COMPLETE_CONTENT, 0.8);
        List<String> keywords = Arrays.stream(keywordStr.split(",")).map(e -> e.replaceAll("[^가-힣0-9 ]", "").trim()).toList();
        log.info("\n[키워드]\n{}", keywords);
//        List<String> keywords = List.of("공기가 맑은 곳", "안전한 곳");

        // 전체 대화 내용을 기반으로 GPT 응답 반환 (조건 - 데이터 매칭)
//        String gptResponse = chatGPTServiceImpl.getGptOutputComplete(conversation.toString(), keywords);
        String gptResponse = "공기가 맑은 곳@방향-북\n" +
                "공기가 맑은 곳@CCTV,공기가 맑은 곳@경비원,공기가 맑은 곳@현관보안\n" +
                "공기가 맑은 곳@병원_all-5\n" +
                "공기가 맑은 곳@감염병율-5,안전한 곳@범죄율-8\n";
        log.info("\n<GPT 응답>\n{}", gptResponse);
        // 매물 점수 계산해서 가져오기
        String userId = securityService.getUserId(httpRequest);
        List<HouseWithCondition> resultHouses = conditionService.exec(manConRequest, gptResponse, keywords, session, userId);

        // 세션에 저장
        session.setAttribute(SessionKeys.HOUSE_RESULTS_KEY, resultHouses);

        // 결과 반환
        if (resultHouses.isEmpty()) {
            return new ResponseEntity<>(new SearchResponse(new ArrayList<>(), true, 200, "No Content"), HttpStatus.OK);
        } else {
            // log 출력 for문
            for (int i = 0; i < Math.min(resultHouses.size(), 5); i++) {
                House house = resultHouses.get(i).getHouse();
                log.info("최종 결과 - 매물id: {} / 총 점수: {} / 공공 데이터 점수: {} / 시설 데이터 점수: {}", house.getHouseId(), house.getScore(), house.getPublicDataScore(), house.getFacilityDataScore());
            }

            return new ResponseEntity<>(new SearchResponse(resultHouses, true, 200, "성공"), HttpStatus.OK);
        }
    }
}

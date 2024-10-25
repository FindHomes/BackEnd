package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.condition.service.ConditionService;
import com.findhomes.findhomesbe.condition.service.HouseWithConditionService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.exception.exception.ClientIllegalArgumentException;
import com.findhomes.findhomesbe.gpt.ChatGPTServiceImpl;
import com.findhomes.findhomesbe.login.JwtTokenProvider;
import com.findhomes.findhomesbe.login.SecurityService;
import com.findhomes.findhomesbe.repository.FavoriteHouseRepository;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    public static final String ALL_CONDITIONS = "all-conditions";

    private final ChatGPTServiceImpl chatGPTServiceImpl;
    private final UserChatService userChatService;
    private final ConditionService conditionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityService securityService;
    private final HouseRepository houseRepository;
    private final HouseWithConditionService houseWithConditionService;
    private final HouseService houseService;
    private final FavoriteHouseService favoriteHouseService;
    private final RecentlyViewedHouseService recentlyViewedHouseService;
    private final FavoriteHouseRepository favoriteHouseRepository;

    @PostMapping("/api/search/man-con")
    public ResponseEntity<ManConResponse> setManConSearch(
            @RequestBody ManConRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        HttpSession session = securityService.getNewSession(httpRequest);
        session.setAttribute(MAN_CON_KEY, request);

        String sessionId = session.getId();
        securityService.addSessionIdOnCookie(sessionId, response);

        // 추천 질문 생성
        String command = createUserConditionCommand(conditionService.conditionsToSentence());
        String gptOutput = chatGPTServiceImpl.getGptOutput(command, ROLE1, ROLE2, COMPLETE_CONTENT, USER_CONDITION_TEMPERATURE);

        // 응답 반환
        ManConResponse responseBody = new ManConResponse(true, 200, "필수 조건이 잘 저장되었습니다.", Arrays.stream(gptOutput.split("\n")).map(str -> str.replaceAll("[^가-힣0-9a-zA-Z .,]", "").trim()).filter(str -> !str.isEmpty()).collect(Collectors.toList()));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/api/search/user-chat")
    @Operation(summary = "사용자 채팅", description = "사용자 입력을 받고, 챗봇의 응답을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "챗봇 응답 완료", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserChatResponse.class))})
    @ApiResponse(responseCode = "204", description = "챗봇 대화 종료", content = {@Content(mediaType = "application/json")})
    public ResponseEntity<UserChatResponse> userChat(
            @RequestBody UserChatRequest userChatRequest,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @SessionAttribute(value = MAN_CON_KEY, required = false) ManConRequest manConRequest
    ) {
        HttpSession session = securityService.getSession(httpRequest);
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
        String command = createChatCommand(userChatRequest.getUserInput(), FacilityCategory.getAllData() + PublicData.getAllData());
        conversation.append(command);

        // GPT에게 요청 보내기 (여기서 gptService를 사용하여 GPT 응답을 가져옵니다)
        String gptResponse = chatGPTServiceImpl.getGptOutput(conversation.toString(), ROLE1, ROLE2, CHAT_CONTENT, CHAT_TEMPERATURE).replaceAll("[^가-힣,.!? ]", "").trim();
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
    @Operation(summary = "조건 입력 완료", description = "조건 입력을 완료하고 매물을 반환받습니다.\n\n" + "최대 100개의 매물을 점수를 기준으로 내림차순으로 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매물 응답 완료"),
            @ApiResponse(responseCode = "401", description = "session이 없습니다. 필수 조건 입력 창으로 돌아가야 합니다."),
            @ApiResponse(responseCode = "428", description = "세션에 필수 데이터가 없습니다.")})
    public ResponseEntity<SearchResponse> getHouseList(
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @SessionAttribute(value = MAN_CON_KEY, required = false) ManConRequest manConRequest
    ) {
        System.out.println(manConRequest);
        HttpSession session = securityService.getSession(httpRequest);
        String chatSessionId = session.getId();

        // 이전 대화 내용을 가져오기
        List<UserChat> previousChats = userChatService.getUserChatsBySessionId(chatSessionId);
        StringBuilder conversation = new StringBuilder();
        for (UserChat chat : previousChats) {
            conversation.append("사용자: ").append(chat.getUserInput()).append("\n");
        }
        // 대화에서 키워드 추출 하기
        // TODO: gpt 아낄라고 임시로 이렇게 해놓음 수정해야됨.
        String input = conversation.toString() + "\n" + EXTRACT_KEYWORD_COMMAND;
        String keywordStr = chatGPTServiceImpl.getGptOutput(input, ROLE1, ROLE2, COMPLETE_CONTENT, 0.8);
        List<String> keywords = Arrays.stream(keywordStr.split(",")).map(e -> e.replaceAll("[^가-힣0-9 ]", "").trim()).toList();
        log.info("\n[키워드]\n{}", keywords);
//        List<String> keywords = List.of("공기가 맑은 곳", "안전한 곳");

        // 전체 대화 내용을 기반으로 GPT 응답 반환 (조건 - 데이터 매칭)
        String gptResponse = chatGPTServiceImpl.getGptOutputComplete(conversation.toString(), keywords);
        // TODO: gpt 아낄라고 임시로 이렇게 해놓음 수정해야됨.
//        String gptResponse = "공기가 맑은 곳@방향-북\n" +
//                "공기가 맑은 곳@CCTV,공기가 맑은 곳@경비원,공기가 맑은 곳@현관보안\n" +
//                "공기가 맑은 곳@병원_all-5\n" +
//                "공기가 맑은 곳@감염병율-5,안전한 곳@범죄율-8\n";
        log.info("\n<GPT 응답>\n{}", gptResponse);
        // 매물 점수 계산해서 가져오기
        String userId = securityService.getUserId(httpRequest);
        List<HouseWithCondition> resultHouses = conditionService.exec(manConRequest, gptResponse, keywords, session, userId);
        // 세션에 저장
        session.setAttribute(HOUSE_RESULTS_KEY, resultHouses);

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

    @GetMapping("/api/search/statistics")
    @Operation(summary = "통계 정보 가져오기", description = "현재 결과에 반영된 데이터 정보를 가져옵니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "매물 응답 완료"), @ApiResponse(responseCode = "401", description = "세션이 유효하지 않습니다"),})
    public ResponseEntity<StatisticsResponse> getStatistics(
            @Parameter(hidden = true) @SessionAttribute(value = HOUSE_RESULTS_KEY, required = false) List<HouseWithCondition> houseWithConditions,
            @Parameter(hidden = true) @SessionAttribute(value = ALL_CONDITIONS, required = false) AllConditions allConditions
    ) {
        return new ResponseEntity<>(StatisticsResponse.of(houseWithConditions, allConditions, true, 200, "응답 성공"), HttpStatus.OK);
    }


    // 최근 본 매물 조회 API
    @GetMapping("/api/houses/recently-viewed")
    @Operation(summary = "최근 본 매물", description = "사용자가 최근에 본 매물을 최신순으로 반환합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공적으로 최근 본 매물을 반환함"), @ApiResponse(responseCode = "401", description = "인증 오류"), @ApiResponse(responseCode = "404", description = "최근 본 매물이 없습니다")})
    public ResponseEntity<Response> getRecentlyViewedHouses(HttpServletRequest httpRequest) {
        String userId = securityService.getUserId(httpRequest);
        List<ResponseHouse> recentlyViewedHouses = recentlyViewedHouseService.getRecentlyViewedHouses(userId).stream().map(house -> new ResponseHouse(house,true)).collect(Collectors.toList());;
        // 최근 본 매물이 없는 경우, 빈 리스트 반환
        if (recentlyViewedHouses.isEmpty()) {
            return new ResponseEntity<>(new Response(true, 200, "최근 본 매물이 없습니다", Collections.emptyList()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Response(true, 200,"최근 본 매물을 불러오는데 성공하였습니다",recentlyViewedHouses), HttpStatus.OK);
    }

    // 찜한 방 매물 조회 API
    @GetMapping("/api/houses/favorite")
    @Operation(summary = "찜한 방 ", description = "사용자가 찜한 매물을 반환합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공적으로 찜한 매물을 반환함"), @ApiResponse(responseCode = "401", description = "인증 오류"), @ApiResponse(responseCode = "404", description = "찜한 방이 없습니다")})
    public ResponseEntity<Response> getfavoriteHouses(HttpServletRequest httpRequest) {
        String userId = securityService.getUserId(httpRequest);
        List<ResponseHouse> favoriteHouses = favoriteHouseService.getFavoriteHouses(userId).stream().map(house -> new ResponseHouse(house,true)).collect(Collectors.toList());
        // 찜한 방이 없는 경우, 빈 리스트 반환
        if (favoriteHouses.isEmpty()) {
            return new ResponseEntity<>(new Response(true, 200, "찜한 방이 없습니다", Collections.emptyList()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Response(true, 200,"찜한 매물을 불러오는데 성공하였습니다",favoriteHouses), HttpStatus.OK);
    }

    // 찜하기 API
    @PostMapping("/api/houses/{houseId}/favorite")
    @Operation(summary = "찜하기", description = "찜하기 버튼을 눌러 찜을 등록하거나 해제합니다. action 파라미터로는 add 또는 remove 값으로 찜 등록 및 해제를 구분합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "찜하기 처리 완료"), @ApiResponse(responseCode = "404", description = "입력 id에 해당하는 매물이 없습니다")})
    public ResponseEntity<HouseDetailResponse> manageFavoriteOnHouse(HttpServletRequest httpRequest, @PathVariable("houseId") int houseId, @RequestParam("action") String action) {
        String userId = securityService.getUserId(httpRequest);
        if (action.equalsIgnoreCase("add")) {
            favoriteHouseService.addFavoriteHouse(userId, houseId);
        } else if (action.equalsIgnoreCase("remove")) {
            favoriteHouseService.removeFavoriteHouse(userId, houseId);
        } else {
            throw new ClientIllegalArgumentException("잘못된 action 값입니다. add 또는 remove를 사용하세요.");
        }
        return new ResponseEntity<>(new HouseDetailResponse(true, 200, "찜하기 처리 완료"), HttpStatus.OK);

    }

    // 매물 상세페이지 API
    @GetMapping("/api/houses/{houseId}")
    @Operation(summary = "매물 상세페이지", description = "매물을 클릭하고 상세페이지로 이동합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "매물 응답 완료"), @ApiResponse(responseCode = "401", description = "유효한 session이 없습니다. 필수 조건 입력 창으로 돌아가야 합니다."), @ApiResponse(responseCode = "404", description = "입력 id에 해당하는 매물이 없습니다")})
    public ResponseEntity<HouseDetailResponse> getHouseDetail(HttpServletRequest httpRequest, @PathVariable int houseId) {
        String userId = securityService.getUserId(httpRequest);
        // 최근 본 방에 추가
        recentlyViewedHouseService.saveOrUpdateRecentlyViewedHouse(userId, houseId);
        HouseDetailResponse houseDetailResponse = favoriteHouseService.getHouseDetailwithFavoriteFlag(userId, houseId);
        return new ResponseEntity<>(houseDetailResponse, HttpStatus.OK);
    }
}

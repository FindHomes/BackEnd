package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.*;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.condition.service.ConditionService;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.UserChat;
import com.findhomes.findhomesbe.login.JwtTokenProvider;
import com.findhomes.findhomesbe.repository.HouseRepository;
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
        String gptOutput = chatGPTServiceImpl.getGptOutput(makeUserRecommendInput(request), 0.9);

        // 응답 반환
        ManConResponse responseBody = new ManConResponse(true, 200, "필수 조건이 잘 저장되었습니다.", Arrays.stream(gptOutput.split("\n")).map(str -> str.replaceAll("^가-힣", "").trim()).collect(Collectors.toList()));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }


    private String makeUserRecommendInput(ManConRequest request) {
        // TODO: 여기에 유저 정보도 넣어 줘야 할 듯.
        return "[보유 데이터 목록]\n" + conditionService.conditionsToSentence() + "\n" +
                "위의 데이터를 참고해서 유저에게 매물을 찾을 때 입력할 조건을 추천해줘.\n" +
                "이 서비스는 보유 데이터 목록을 기반으로 유저가 입력한 조건에 맞는 부동산 매물을 찾아주는 서비스야.\n" +
                "문장의 길이를 100자 안으로 해서 다음 조건의 3개의 문장을 랜덤하게 추천해줘.\n" +
                "문장 한 개는 보유 데이터 목록에서 참고해서 추천해주는 문장이어야 돼." +
                "나머지 문장 두 개는 **보유 데이터를 절대 직접 언급하면 안되고**, " +
                "보유 데이터와 간접적인 연관이 있고 아주 색다른 조건이 있는 문장이어야 돼.\n" +
                "그리고 세 문장 모두 두 개 이상의 조건이 들어가야돼.\n" +
                "예시: CCTV가 있고, 복층 구조로 추천해줘.\\n어르신들이 살기 좋고 벌레가 없는 곳으로 추천해줘.\\n아이 키우기 좋고 안전한 곳으로 추천해줘.\n" +
                "각 문장은 개행문자로 구분해서 한 줄에 하나의 문장만 나오게 해줘. 그 외에 다른 말은 아무것도 붙이지 말아줘. 특히 문장에 Escape Character 절대로 쓰지 말아줘. 개행문자에 Escape Character 두개 연속으로 절대 쓰지마.\n" +
                "문장에 교통 관련 조건은 절대 있으면 안돼.\n" +
                "문장에 보유 데이터에 없는 집 내부의 가구나 옵션이 절대 있으면 안돼.";
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

        // 사용자 입력 추가 및 대화 응답 조정
        conversation.append("사용자: ").append(userChatRequest.getUserInput()).append("\n").append("사전에 사용자 입력한 매물 조건 :").append(manConRequest.toSentence()).append("너는 사용자가 사전에 입력한 조건을 고려해서 원하는 다른 조건이 있는지 물어보는 응답을 해야해.").append("제안할 수 있는 조건 종류 :").append(FacilityCategory.getAllData() + PublicData.getAllData()).append(" 만약 한번 조건 추가 입력 요구 제안을 했으면 다시 제안하지 않고 대화를 끝내고 매물을 찾아주겠다는 느낌으로 응답을 해도 돼. 추가로 대화를 끝내려면 대화 종료 버튼을 눌러서 대화를 끝내고 매물을 찾을 수 있다고 사용자에게 알려줘. 또한 **이나 개행문자가 없는 순수 string으로 응답해줘.");

        // GPT에게 요청 보내기 (여기서 gptService를 사용하여 GPT 응답을 가져옵니다)
        String gptResponse = chatService.getResponse(conversation.toString());
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
        log.info(chatGPTServiceImpl.createGPTCommand(conversation.toString()));
        // 전체 대화 내용을 기반으로 GPT 응답 반환 (조건 - 데이터 매칭)
        String gptResponse = chatGPTServiceImpl.getGptOutput(chatGPTServiceImpl.createGPTCommand(conversation.toString()), 0.1);
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

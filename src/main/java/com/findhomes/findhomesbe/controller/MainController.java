package com.findhomes.findhomesbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.findhomes.findhomesbe.DTO.SearchResponse.Response.Ranking;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    static String usertestInput = "집 가까이 버거킹이 있고 치안이 좋았으면 좋겠어"; // 테스트 입력, 여기에 유저의 입력문장을 적는다.
    private final ChatGPTService chatGPTService;
    // 조건 입력 받기
    @PostMapping("/api/search")
    public ResponseEntity<SearchResponse> search(@org.springframework.web.bind.annotation.RequestBody SearchRequest request) throws IOException {
        SearchRequest.ManCon manCon = request.getManCon();
        List<String> housingTypes = manCon.getHousingTypes();
        int mm = manCon.getPrices().getMm();
        int js = manCon.getPrices().getJs();
        int deposit = manCon.getPrices().getWs().getDeposit();
        int rent = manCon.getPrices().getWs().getRent();
        List<String> regions = manCon.getRegions();
        String userInput = request.getUserInput();
        // 키워드
        String keywords = keyword();
        String command = "유저의 입력 문장과 현재 우리가 보유한 데이터 목록을 너한테 줄꺼야. 너는 문장과 연관이 있는 데이터를 선정한 뒤 각 데이터에 가중치를 설정하고 그 결과를 나한테 반환해주면 돼";
        command +="예를 들어 '집 가까이 버거킹이 있고 치안이 좋았으면 좋겠어' 라고 문장이 들어오면 버거킹0.4-안전0.6 이렇게 반환해주면 돼. 너는 앞에서 말한 반환양식대로만 문장을 반환해주면 돼. '다음은 반환문장입니다'와 같은 미사어구 넣지마. 다음 문장은 유저입력과 보유 데이터야.\n";
        command += "유저 입력문장:"+userInput +"\n" + "보유 데이터:"+keywords;
        // 메시지 생성
        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a helpful assistant.")
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
        // GPT 호출
        Map<String, Object> result = chatGPTService.prompt(completionRequestDto);
        System.out.println(result);

        // 매물 선정 예시 데이터 (실제 데이터는 gptResult에서 파싱해야 함)
        List<Ranking> rankings = Arrays.asList(
                Ranking.builder()
                        .rank(1)
                        .priceType("매매")
                        .price(20000)
                        .rent(0)
                        .address("경기도 안양시 동안구 어쩌고")
                        .housingType("ONE")
                        .info(Ranking.Info.builder()
                                .floor(3)
                                .size("40평")
                                .build())
                        .build(),
                Ranking.builder()
                        .rank(2)
                        .priceType("월세")
                        .price(2000)
                        .rent(100)
                        .address("서울시 광진구 건국대")
                        .housingType("TWO")
                        .info(Ranking.Info.builder()
                                .floor(3)
                                .size("40평")
                                .build())
                        .build()
        );

        SearchResponse.Response response = SearchResponse.Response.builder()
                .rankings(rankings)
                .build();

        SearchResponse searchResponse = SearchResponse.builder()
                .response(response)
                .build();

        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
    }


    // GPT API 테스트 컨트롤러
    @PostMapping("/prompt")
    public ResponseEntity<Map<String, Object>> selectPrompt() {

        // 키워드
        String keywords = keyword();
        String command = "유저의 입력 문장과 현재 우리가 보유한 데이터 목록을 너한테 줄꺼야. 너는 문장과 연관이 있는 데이터를 선정한 뒤 각 데이터에 가중치를 설정하고 그 결과를 나한테 반환해주면 돼";
        command +="예를 들어 '집 가까이 버거킹이 있고 치안이 좋았으면 좋겠어' 라고 문장이 들어오면 버거킹0.4-안전0.6 이렇게 반환해주면 돼. 너는 앞에서 말한 반환양식대로만 문장을 반환해주면 돼. '다음은 반환문장입니다'와 같은 미사어구 넣지마. 다음 문장은 유저입력과 보유 데이터야.\n";
        command += "유저 입력문장:"+usertestInput +"\n" + "보유 데이터:"+keywords;

        // 여러 메시지를 포함한 요청 생성
        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a helpful assistant.")
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

        // GPT 호출
        Map<String, Object> result = chatGPTService.prompt(completionRequestDto);

        System.out.println(result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }



    public String keyword() {
        String keyword = "버거킹";
        keyword += ",CCTV";
        keyword += ", 범죄율";

        return keyword;
    }



}

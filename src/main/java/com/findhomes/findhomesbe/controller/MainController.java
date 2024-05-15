package com.findhomes.findhomesbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.SearchRequest;
import com.findhomes.findhomesbe.service.ChatGPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    static String usertestInput = "쇼핑하기 좋고 자녀 교육에 좋은 집 추천해줘"; // 테스트 입력, 여기에 유저의 입력문장을 적는다.
    private final ChatGPTService chatGPTService;
    // 조건 입력 받기
    @PostMapping("/api/search")
    public String search(@org.springframework.web.bind.annotation.RequestBody SearchRequest request) throws IOException {
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
        String command = "유저의 입력문장과 키워드-데이터 매칭관계를 너에게 줄꺼야. 너는 입력문장에서 관련된 키워드를 고르고 그 키워드와 연관된 데이터와 가중치를 내게 반환해주면 돼.";
        command +="'예를 들어 살기 좋은 집 추천해줘' 라고 문장이 들어오면 '미세먼지0.5-범죄0.2-생활안전0.2-화재0.1' 이렇게 반환해주면 돼. '입력문장에서 관련된 키워드를 찾고 해당 키워드와 연관된 데이터와 가중치를 반환해 드릴게요.'같은 문장쓰지말고 내가 앞에 보여준 양식 그대로 반환양식 꼭 지켜서 반환해줘. 그러니까 데이터-가중치 딱 이런 양식으로 반환해줘 다음은 유저입력과 키워드-데이터 관계야 \n";
        command += "유저 입력문장:"+userInput +"\n" + "키워드-데이터 매칭관계:"+keywords;
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

        // 매물 선정

        return null;
    }


    // 테스트 컨트롤러
    @PostMapping("/prompt")
    public ResponseEntity<Map<String, Object>> selectPrompt() {

        // 키워드
        String keywords = keyword();
        String command = "유저의 입력문장과 키워드-데이터 매칭관계를 너에게 줄꺼야. 너는 입력문장에서 관련된 키워드를 고르고 그 키워드와 연관된 데이터와 가중치를 내게 반환해주면 돼.";
        command +="'예를 들어 살기 좋은 집 추천해줘' 라고 문장이 들어오면 미세먼지0.5-범죄0.2-생활안전0.2-화재0.1 이렇게 반환해주면 돼. 반환양식 꼭 지켜서 반환해줘 다음은 유저입력과 키워드-데이터 관계야 \n";
        command += "유저 입력문장:"+usertestInput +"\n" + "키워드-데이터 매칭관계:"+keywords;

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
        String keyword = "살기 좋은:미세먼지0.5-범죄0.2-생활안전0.2-화재0.1";
        keyword += "안전한 집:교통사고0.3-화재0.3-범죄0.3-생활안전0.2-CCTV0.1";
        keyword += "공기 좋은 집:공기질1.0";
        keyword += "편의시설 많은 집:백화점0.4-대형마트0.4-슈퍼마켓0.2";
        keyword += "교통 편리한 집:지하철역0.6-버스정류장0.4";
        keyword += "자연과 가까운 집:공원1.0";
        keyword += "교육 환경 좋은 집:학교0.5-학원0.5";
        keyword += "의료시설 가까운 집:병원0.7-의원0.3";
        keyword += "공원 근처:공원1.0";
        keyword += "쇼핑 편리한:백화점0.4-대형마트0.4-체인화편의점0.2";
        keyword += "문화시설 많은:서적소매업0.3-영화관0.4-공연시설0.3";
        keyword += "자녀 교육 좋은:초등학교0.4-중학교0.3-학원0.3";
        keyword += "건강관리 편리한:병원0.5-의원0.5";
        keyword += "산책로 가까운:공원0.7-자연공원0.3";
        keyword += "아이들 키우기 좋은:어린이집0.5-공원0.5";
        keyword += "다양한 음식점 많은:음식점업1.0";
        keyword += "저렴한 집:가격분석기반0.5";
        keyword += "물가 낮은 지역:슈퍼마켓0.5-전통시장0.5";
        keyword += "뷰가 좋은 집:자연공원0.6-강호수0.4";
        keyword += "애완동물 친화적인:애완동물시설1.0";
        keyword += "체육시설 많은:체력단련시설0.5-수영장0.3-골프연습장0.2";
        keyword += "수영장 있는:수영장1.0";
        keyword += "편의점 많은:체인화편의점1.0";
        keyword += "전통시장 근처:전통시장1.0";
        keyword += "자전거 도로 가까운:자전거도로1.0";
        keyword += "아파트 단지 많은:아파트단지1.0";
        keyword += "새 아파트 많은:신축아파트1.0";
        keyword += "전원주택 많은:전원주택1.0";
        keyword += "관공서 가까운:관공서1.0";
        keyword += "비즈니스 중심지:오피스빌딩0.7-비즈니스서비스0.3";
        keyword += "스타벅스 근처:스타벅스1.0";
        keyword += "영화관 가까운:영화관1.0";
        keyword += "은행 많은:일반은행1.0";
        keyword += "헬스장 많은:체력단련시설1.0";
        keyword += "유흥업소 적은:유흥업소1.0";
        keyword += "음식 배달 편리한:음식배달서비스1.0";
        keyword += "마트 많은:대형마트0.6-슈퍼마켓0.4";
        keyword += "식당 많은:일반음식점1.0";
        keyword += "카페 많은:커피전문점1.0";
        keyword += "노래방 많은:노래연습장1.0";
        keyword += "대형마트 가까운:대형마트1.0";
        keyword += "공원과 체육시설 많은:공원0.6-체력단련시설0.4";
        return keyword;
    }



}

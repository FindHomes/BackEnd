package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    // 조건 입력 받기
    @PostMapping("/api/search")
    public String search(@RequestBody SearchRequest request) {
        SearchRequest.ManCon manCon = request.getManCon();
        List<String> housingTypes = manCon.getHousingTypes();
        int mm = manCon.getPrices().getMm();
        int js = manCon.getPrices().getJs();
        int deposit = manCon.getPrices().getWs().getDeposit();
        int rent = manCon.getPrices().getWs().getRent();
        List<String> regions = manCon.getRegions();
        String userInput = request.getUserInput();
        // 키워드 사전 정의
        
        
        // GPT 호출

        
        // 매물 선정


        // 예시 출력
        System.out.println("Housing Types: " + housingTypes);
        System.out.println("Prices: mm=" + mm + ", js=" + js + ", deposit=" + deposit + ", rent=" + rent);
        System.out.println("Regions: " + regions);
        System.out.println("User Input: " + userInput);

        return "Hello";
    }
    public String keyword() {
        String keyword = "살기 좋은:미세먼지0.5-범죄0.2-생활안전0.2-화재0.1";
        keyword += "안전한 집:교통사고0.3-화재0.3-범죄0.3-생활안전0.2-CCTV0.1";
        keyword += "공기 좋은 집:공기질1.0";
        keyword += "편의시설 많은 집:백화점0.4-대형마트0.4-슈퍼마켓0.2";
        keyword += "교통 편리한 집:지하철역0.6-버스정류장0.4";
        keyword += "조용한 집:교통사고0.2-유흥업소0.5-공기질0.3";
        keyword += "자연과 가까운 집:공원1.0";
        keyword += "교육 환경 좋은 집:학교0.5-학원0.5";
        keyword += "의료시설 가까운 집:병원0.7-의원0.3";
        keyword += "조용한 주거지:교통사고0.2-유흥업소0.5-공기질0.3";
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
        keyword += "강 근처:강호수1.0";
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

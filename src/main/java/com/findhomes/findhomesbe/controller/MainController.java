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

        // 실제 로직 처리
        // 예시 출력
        System.out.println("Housing Types: " + housingTypes);
        System.out.println("Prices: mm=" + mm + ", js=" + js + ", deposit=" + deposit + ", rent=" + rent);
        System.out.println("Regions: " + regions);
        System.out.println("User Input: " + userInput);

        return "Hello";
    }

}

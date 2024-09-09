package com.findhomes.findhomesbe.controller;

import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Stream;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test/api/search")
public class TestController {
    private static List<House> tempResult;
    private final HouseRepository houseRepository;

    @PostConstruct
    public void init() {
        tempResult = houseRepository.findByHousingType("아파트");
        log.info("임시 데이터 매물 개수: {}", tempResult.size());
    }

    @GetMapping("/complete")
    @Operation(summary = "임시 데이터 반환 - 조건 입력 완료", description = "그냥 호출하면 테스트 데이터가 넘어옵니다. 사전에 다른거 호출할 필요 x")
    @ApiResponse(responseCode = "200", description = "매물 응답 완료")
    public ResponseEntity<SearchResponse> getHouseList(HttpServletRequest httpRequest) {
        Collections.shuffle(tempResult);
        Random rand = new Random();
        int subListSize = rand.nextInt(tempResult.size()) + 1;

        List<House> subList = tempResult.subList(0, subListSize);

        SearchResponse response = new SearchResponse(subList.subList(0, Math.min(100, subList.size())), true, 200, "성공");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

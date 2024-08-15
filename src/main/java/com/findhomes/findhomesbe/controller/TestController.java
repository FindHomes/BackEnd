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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
        tempResult = houseRepository.findByPriceTypeAndHousingType("월세", "원룸");
        for (int i = 0; i < tempResult.size(); i++) {
            double score = tempResult.size() - i;
            tempResult.get(i).setScore(score);
        }
        log.info("임시 데이터 매물 개수: {}", tempResult.size());
    }

    @GetMapping("/complete")
    @Operation(summary = "임시 데이터 반환 - 조건 입력 완료", description = "그냥 호출하면 테스트 데이터가 넘어옵니다. 사전에 다른거 호출할 필요 x")
    @ApiResponse(responseCode = "200", description = "매물 응답 완료")
    public ResponseEntity<SearchResponse> getHouseList(HttpServletRequest httpRequest) {
        SearchResponse.SearchResult searchResult = new SearchResponse.SearchResult();
        Random rand = new Random();
        int index1 = rand.nextInt(tempResult.size());
        int index2 = rand.nextInt(tempResult.size());
        while (Objects.equals(tempResult.get(index1).getX(), tempResult.get(index2).getX())) {
            index2 = rand.nextInt(tempResult.size());
        }
        House randomHouse1 = tempResult.get(index1);
        House randomHouse2 = tempResult.get(index2);
        double x1 = randomHouse1.getX();
        double y1 = randomHouse1.getY();
        double x2 = randomHouse2.getX();
        double y2 = randomHouse2.getY();
        if (x1 > x2) {
            searchResult.setXMax(x1);
            searchResult.setXMin(x2);
        } else {
            searchResult.setXMax(x2);
            searchResult.setXMin(x1);
        }
        if (y1 > y2) {
            searchResult.setYMax(y1);
            searchResult.setYMin(y2);
        } else {
            searchResult.setYMax(y2);
            searchResult.setYMin(y1);
        }

        List<House> result = tempResult.stream()
                .filter(house -> house.getX() >= searchResult.getXMin() && house.getX() <= searchResult.getXMax() && house.getY() <= searchResult.getYMax() && house.getY() >= searchResult.getYMin())
                .toList();
        searchResult.setHouses(result.subList(0, Math.min(100, result.size())));

        SearchResponse response = new SearchResponse(true, 200, 200, "성공", searchResult);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /*@GetMapping("/update")
    @Operation(summary = "임시 데이터 반환 - 사용자 지도 상호작용 시 매물 리스트 갱신", description = "그냥 호출하면 테스트 데이터가 넘어옵니다. 사전에 다른거 호출할 필요 x")
    @ApiResponse(responseCode = "200", description = "매물 리스트를 반환합니다.")
    public ResponseEntity<List<House>> getUpdatedHouseList(
            @RequestParam @Parameter(description = "경도 최댓값") double xMax,
            @RequestParam @Parameter(description = "경도 최솟값") double xMin,
            @RequestParam @Parameter(description = "위도 최댓값") double yMax,
            @RequestParam @Parameter(description = "위도 최솟값") double yMin
    ) {
        List<House> result = tempResult.stream()
                .filter(house -> house.getX() >= xMin && house.getX() <= xMax && house.getY() <= yMax && house.getY() >= yMin)
                .toList();
        return new ResponseEntity<>(result.subList(0, Math.min(100, result.size())), HttpStatus.OK);
    }*/
}

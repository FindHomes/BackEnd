package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.condition.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {
    private final ParsingService parsingService;
    private final HouseService houseService;
    private final HouseWithConditionService houseWithConditionService;

    public SearchResponse exec(ManConRequest manConRequest, String gptOutput) {
        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput);
        log.info("\n===========조건 파싱 결과===========\n{}", allConditions);

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)
        List<House> houses = houseService.getHouseByAllConditions(allConditions);
        //
        List<HouseWithCondition> houseWithConditions = houseWithConditionService.convertHouseList(houses);

        // 2. 공공 데이터 조건 처리
        houseWithConditionService.injectPublicDataInList(houseWithConditions, allConditions.getPublicConditionDataList());

        log.info("{}", houseWithConditions);

        // 3. 시설 조건 및 사용자 요청 위치 조건 처리


        SearchResponse.SearchResult searchResult = new SearchResponse.SearchResult();
        searchResult.setHouses(houses);
        return new SearchResponse(true, 200, "성공", searchResult);
    }

}

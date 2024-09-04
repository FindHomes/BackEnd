package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {
    private final ParsingService parsingService;
    private final HouseService houseService;
    private final HouseWithConditionService houseWithConditionService;
    private final PublicDataService publicDataService;
    private final IndustryService industryService;

    public List<House> exec(ManConRequest manConRequest, String gptOutput) {
        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput);
        log.info("\n===========조건 파싱 결과===========\n{}", allConditions);

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)
        List<House> houses = houseService.getHouseByAllConditions(allConditions);
        // HouseWithCondition 리스트로 바꿔주기
        List<HouseWithCondition> houseWithConditions = houseWithConditionService.convertHouseList(houses);

        // 2. 공공 데이터 조건 처리
        publicDataService.injectPublicDataInList(houseWithConditions, allConditions.getPublicConditionDataList());
        // 공공 데이터 처리 결과 출력
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            log.info("매물id: {}, 주소: {}, 등급 정보: {}", houseWithCondition.getHouse().getHouseId(), houseWithCondition.getHouse().getAddress(), houseWithCondition.getSafetyGradeInfoList());
        }

        // 3. 시설 조건 및 사용자 요청 위치 조건 처리
        List<IndustriesAndWeight> industriesAndWeights = industryService.injectFacilityDataInList(allConditions.getFacilityConditionDataList());

        // 4. 점수 계산
        houseWithConditionService.calculate(houseWithConditions, industriesAndWeights);

        // 5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬
        houseWithConditionService.sort(houseWithConditions);

        // 반환
        return houseWithConditionService.convertToHouseList(houseWithConditions);
    }

}

package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.RegionsRepository;
import com.findhomes.findhomesbe.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

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
    private final RegionsRepository regionsRepository;
    public List<House> exec(ManConRequest manConRequest, String gptOutput) {
        ManConRequest.Region region = manConRequest.getRegion();

        
        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput);
        log.info("\n===========조건 파싱 결과===========\n{}", allConditions);

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)
        List<House> houses = houseService.getHouseByAllConditions(allConditions);
        // HouseWithCondition 리스트로 바꿔주기
        List<HouseWithCondition> houseWithConditions = houseWithConditionService.convertHouseList(houses);
        log.info("1. 필터링 조건으로 매물 필터링해서 매물 가져오기 완료. 매물 개수: {}", houseWithConditions.size());
        // 2. 공공 데이터 조건 처리
        long startTime2 = System.currentTimeMillis();
        publicDataService.injectPublicDataInList(houseWithConditions, allConditions.getPublicConditionDataList());
        long endTime2 = System.currentTimeMillis();
        log.info("2. 공공데이터 조건 처리 완료");
        /*// 공공 데이터 처리 결과 출력
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            log.info("매물id: {}, 주소: {}, 등급 정보: {}", houseWithCondition.getHouse().getHouseId(), houseWithCondition.getHouse().getAddress(), houseWithCondition.getSafetyGradeInfoList());
        }*/
        log.info("2. 공공 데이터 조건 처리 완료, 소요시간: " + (endTime2 - startTime2) / 1000.0 + "초");
        // 3. 시설 조건 및 사용자 요청 위치 조건 처리
        long startTime3 = System.currentTimeMillis();
        List<IndustriesAndWeight> industriesAndWeights = industryService.injectFacilityDataInList(allConditions.getFacilityConditionDataList(),region);
        long endTime3 = System.currentTimeMillis();
        log.info("3. 시설조건 및 사용자 요청 위치 조건 처리 완료, 소요시간: " + (endTime3 - startTime3) / 1000.0 + "초");

        // 4. 점수 계산
        long startTime4 = System.currentTimeMillis();
        houseWithConditionService.calculate(houseWithConditions, industriesAndWeights);
        long endTime4 = System.currentTimeMillis();
        log.info("4. 점수 계산 완료, 소요시간: " + (endTime4 - startTime4) / 1000.0 + "초");


        // 5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬
        houseWithConditionService.sort(houseWithConditions);
        log.info("5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬 완료");

        // 반환
        return houseWithConditionService.convertToHouseList(houseWithConditions);
    }

    // 보유 데이터를 문장으로 반환
    public String conditionsToSentence() {
        StringBuilder sb = new StringBuilder();

        sb.append("보유 매물 관련 데이터: ");
        sb.append(HouseCondition.getAllData());
        sb.append("\n보유 매물 옵션 데이터: ");
        sb.append(HouseOption.getAllData());
        sb.append("\n보유 시설 데이터: ");
        sb.append(FacilityCategory.getAllData());

        return sb.toString();
    }

}

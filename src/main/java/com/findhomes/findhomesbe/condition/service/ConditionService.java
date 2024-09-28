package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.RegionsRepository;
import com.findhomes.findhomesbe.service.HouseService;
import com.findhomes.findhomesbe.service.PerformanceUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.findhomes.findhomesbe.controller.MainController.ALL_CONDITIONS;

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

    public List<HouseWithCondition> exec(ManConRequest manConRequest, String gptOutput, List<String> keywords, HttpSession session) {
        ManConRequest.Region region = manConRequest.getRegion();

        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput, keywords);
        session.setAttribute(ALL_CONDITIONS, allConditions);
        log.info("\n===========조건 파싱 결과===========\n{}", allConditions);

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)
        List<House> houses = PerformanceUtil.measurePerformance(
                () -> houseService.getHouseByAllConditions(allConditions),
                "1. 필터링 조건으로 매물 필터링해서 매물 가져오기"
        );

        // HouseWithCondition 리스트로 바꿔주기
        List<HouseWithCondition> houseWithConditions = houseWithConditionService.convertHouseList(houses);
        log.info("1. 필터링 조건으로 매물 필터링해서 매물 가져오기 완료. 매물 개수: {}", houseWithConditions.size());

        // 2. 공공 데이터 조건 처리
        PerformanceUtil.measurePerformance(
                () -> publicDataService.injectPublicDataInList(houseWithConditions, allConditions.getPublicConditionDataList()),
                "2. 공공 데이터 조건 처리"
        );

        // 3. 시설 조건 및 사용자 요청 위치 조건 처리
        List<IndustriesAndWeight> industriesAndWeights = PerformanceUtil.measurePerformance(
                () -> industryService.injectFacilityDataInList(allConditions.getFacilityConditionDataList(), region),
                "3. 시설조건 및 사용자 요청 위치 조건 처리"
        );

        // 4. 점수 계산
        PerformanceUtil.measurePerformance(
                () -> houseWithConditionService.calculate(houseWithConditions, industriesAndWeights),
                "4. 점수 계산"
        );

        // 5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬
        houseWithConditionService.sort(houseWithConditions);
        log.info("5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬 완료");

        // 반환
        return houseWithConditions.subList(0, Math.min(100, houseWithConditions.size()));
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

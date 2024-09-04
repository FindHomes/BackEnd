package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.IndustriesAndWeight;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndustryService {
    public final FacilityCategoryService facilityCategoryService;

    public List<IndustriesAndWeight> injectFacilityDataInList(List<AllConditions.FacilityConditionData> facilityConditionDataList) {
        List<IndustriesAndWeight> industriesAndWeights = new ArrayList<>();

        for (AllConditions.FacilityConditionData facilityConditionData : facilityConditionDataList) {
            // gpt가 선별한 facility 항목 하나씩에 대해 해당하는 industry 리스트 가져오기
            List<Industry> newIndustries = facilityCategoryService.getIndustries(facilityConditionData.getFacilityCategoryEnum(), facilityConditionData.getDetailName());
            log.info("카테고리: {} / 상세 요청 키워드: {} / 데이터 개수: {}", facilityConditionData.getFacilityCategoryEnum().name(), facilityConditionData.getDetailName(), newIndustries.size());

            // 응답에 추가하기
            industriesAndWeights.add(new IndustriesAndWeight(newIndustries, facilityConditionData.getWeight()));
        }

        return industriesAndWeights;
    }
}
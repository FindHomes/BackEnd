package com.findhomes.findhomesbe.domain.condition.service;

import com.findhomes.findhomesbe.domain.condition.dto.ManConRequest;
import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.condition.domain.IndustriesAndWeight;
import com.findhomes.findhomesbe.domain.amenities.domain.Amenities;
import com.findhomes.findhomesbe.global.performance.MeasurePerformance;
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
    @MeasurePerformance
    public List<IndustriesAndWeight> injectFacilityDataInList(List<AllConditions.FacilityConditionData> facilityConditionDataList, ManConRequest.Region region) {
        List<IndustriesAndWeight> industriesAndWeights = new ArrayList<>();

        for (AllConditions.FacilityConditionData facilityConditionData : facilityConditionDataList) {
            // gpt가 선별한 facility 항목 하나씩에 대해 해당하는 industry 리스트 가져오기
            log.info("[카테고리: {} / 상세 요청 키워드: {}]", facilityConditionData.getFacilityCategoryEnum().name(), facilityConditionData.getDetailName());
            List<Amenities> newIndustries = facilityCategoryService.getIndustries(facilityConditionData.getFacilityCategoryEnum(), facilityConditionData.getDetailName(), region);
            log.info("데이터 개수: {}", newIndustries.size());

            // 응답에 추가하기
            industriesAndWeights.add(
                    new IndustriesAndWeight(
                            newIndustries, facilityConditionData.getWeight(), facilityConditionData.getMaxRadius(), facilityConditionData)
            );
        }

        return industriesAndWeights;
    }
}

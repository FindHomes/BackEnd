package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
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

    public List<Industry> injectFacilityDataInList(List<HouseWithCondition> houseWithConditions, List<AllConditions.FacilityConditionData> facilityConditionDataList) {
        List<Industry> industries = new ArrayList<>();
        for (AllConditions.FacilityConditionData facilityConditionData : facilityConditionDataList) {
            List<Industry> newIndustries = facilityCategoryService.getIndustries(facilityConditionData.getFacilityCategoryEnum(), facilityConditionData.getDetailName());
            log.info("카테고리: {} , 데이터 개수: {}", facilityConditionData.getFacilityCategoryEnum().name(), newIndustries.size());
            industries.addAll(newIndustries);
        }

        return industries;
    }
}

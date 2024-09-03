package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndustryService {
    public void injectFacilityDataInList(List<HouseWithCondition> houseWithConditions, List<AllConditions.FacilityConditionData> facilityConditionDataList) {
        List<Industry> industries = new ArrayList<>();
        for (AllConditions.FacilityConditionData facilityConditionData : facilityConditionDataList) {
            industries.addAll(getIndustries(facilityConditionData.getFacilityCategoryEnum(), facilityConditionData.getDetailName()));
        }
    }

    private List<Industry> getIndustries(FacilityCategory facilityCategoryEnum, String detailName) {
        return new ArrayList<>();
    }

}

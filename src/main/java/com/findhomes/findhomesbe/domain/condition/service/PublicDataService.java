package com.findhomes.findhomesbe.domain.condition.service;

import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.domain.condition.domain.PublicData;
import com.findhomes.findhomesbe.domain.condition.domain.SafetyGrade;
import com.findhomes.findhomesbe.global.performance.MeasurePerformance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDataService {
    private final SafetyGradeService safetyGradeService;
    @MeasurePerformance
    public void injectPublicDataInList(List<HouseWithCondition> houseWithConditions, List
            <AllConditions.PublicConditionData> publicConditionDataList) {
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            for (AllConditions.PublicConditionData publicConditionData : publicConditionDataList) {
                injectPublicData(houseWithCondition, publicConditionData.getPublicDataEnum(), publicConditionData.getWeight(), publicConditionData.getKeyword());
            }
        }
    }
    private void injectPublicData(HouseWithCondition houseWithCondition, PublicData publicData, Integer weight, String keyword) {

        SafetyGrade safetyGrade = safetyGradeService.getSafetyGradeByAddress(houseWithCondition.getAddressDistrict(), houseWithCondition.getAddressCity());
        if (safetyGrade == null) {
            return;
        }

        switch (publicData.name()) {
            case "교통사고율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        keyword, publicData, weight, safetyGrade.getTrafficAccidents()
                ));
                break;
            case "화재율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        keyword, publicData, weight, safetyGrade.getFire()
                ));
                break;
            case "범죄율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        keyword, publicData, weight, safetyGrade.getCrime()
                ));
                break;
            case "생활안전":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        keyword, publicData, weight, safetyGrade.getPublicSafety()
                ));
                break;
            case "자살율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        keyword, publicData, weight, safetyGrade.getSuicide()
                ));
                break;
            case "감염병율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        keyword, publicData, weight, safetyGrade.getInfectiousDiseases()
                ));
                break;
        }
    }
}

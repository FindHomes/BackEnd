package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.PublicData;
import com.findhomes.findhomesbe.entity.SafetyGrade;
import com.findhomes.findhomesbe.repository.SafetyGradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDataService {
    private final SafetyGradeService safetyGradeService;

    public void injectPublicDataInList(List<HouseWithCondition> houseWithConditions, List
            <AllConditions.PublicConditionData> publicConditionDataList) {
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            for (AllConditions.PublicConditionData publicConditionData : publicConditionDataList) {
                injectPublicData(houseWithCondition, publicConditionData.getPublicDataEnum(), publicConditionData.getWeight());
            }
        }
    }
    private void injectPublicData(HouseWithCondition houseWithCondition, PublicData publicData, Integer weight) {

        SafetyGrade safetyGrade = safetyGradeService.getSafetyGradeByAddress(houseWithCondition.getAddressDistrict(), houseWithCondition.getAddressCity());
        if (safetyGrade == null) {
            return;
        }

        switch (publicData.name()) {
            case "교통사고율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        publicData, weight, safetyGrade.getTrafficAccidents()
                ));
                break;
            case "화재율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        publicData, weight, safetyGrade.getFire()
                ));
                break;
            case "범죄율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        publicData, weight, safetyGrade.getCrime()
                ));
                break;
            case "생활안전":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        publicData, weight, safetyGrade.getPublicSafety()
                ));
                break;
            case "자살율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        publicData, weight, safetyGrade.getSuicide()
                ));
                break;
            case "감염병율":
                houseWithCondition.getSafetyGradeInfoList().add(new HouseWithCondition.SafetyGradeInfo(
                        publicData, weight, safetyGrade.getInfectiousDiseases()
                ));
                break;
        }
    }
}

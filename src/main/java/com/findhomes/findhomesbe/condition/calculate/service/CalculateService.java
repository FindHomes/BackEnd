package com.findhomes.findhomesbe.condition.calculate.service;

import com.findhomes.findhomesbe.condition.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.PublicData;
import com.findhomes.findhomesbe.controller.MainController;
import com.findhomes.findhomesbe.entity.House;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CalculateService {

    public static List<House> calculateScore(List<HouseWithCondition> houseWithConditions, Map<String, Double> facilityMap, Map<PublicData, Double> safetyMap) {
        return houseWithConditions.stream()
                // TODO: 점수 계산 방식 바꿔야 됨.
                .peek(houseWithCondition -> {
                    //log.info("매물번호:{}, 시설 정보:{}, 안전데이터 정보:{}", houseWithCondition.getHouse().getHouseId(), houseWithCondition.getFacilityInfoMap(), houseWithCondition.getSafetyGradeMap());

                    houseWithCondition.getFacilityInfoMap().forEach((key, facilityInfo) -> {
                        Double facilityWeight = facilityMap.get(key);
                        if (facilityWeight != null) {
                            // 해당 시설 개수
                            houseWithCondition.getHouse().addScore(facilityWeight * facilityInfo.getCount());
                            // 해당 시설과의 최단 거리
                            houseWithCondition.getHouse().addScore(facilityWeight * (MainController.RADIUS - facilityInfo.getMinDistance()));
                        }
                    });

                    houseWithCondition.getSafetyGradeMap().forEach((key, value) -> {
                        // 안전등급
                        Double safetyWeight = safetyMap.get(key);
                        if (safetyWeight != null) {
                            houseWithCondition.getHouse().addScore(safetyWeight * (5 - value));
                        }
                    });

                    //log.info("매물번호:{}, 점수:{}", houseWithCondition.getHouse().getHouseId(), houseWithCondition.getHouse().getScore());
                })
                .map(HouseWithCondition::getHouse)
                .sorted(Comparator.comparingDouble(House::getScore).reversed())
                .collect(Collectors.toList());
    }
}

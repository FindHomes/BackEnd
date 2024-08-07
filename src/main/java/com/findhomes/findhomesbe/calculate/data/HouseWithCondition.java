package com.findhomes.findhomesbe.calculate.data;

import com.findhomes.findhomesbe.entity.House;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * since 2024.8.7.
 * 사용자 입력 조건에 대한 매물의 정보를 담고 있습니다.
 * [담고 있는 정보]
 * 1. 매물과 필요 시설과의 거리 정보
 * 2. 매물의 필요 안전 등급 정보
 */
@Data
public class HouseWithCondition {

    private House house;

    private Map<String, FacilityCountDistance> facilityInfoMap = new HashMap<>();
    private Map<SafetyEnum, Double> safetyGradeMap = new HashMap<>();
}

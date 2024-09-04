package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.condition.domain.PublicData;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * since 2024.8.7.
 * 사용자 입력 조건에 대한 매물의 정보를 담고 있습니다.
 * [담고 있는 정보]
 * 1. 매물과 필요 시설과의 거리 정보
 * 2. 매물의 필요 안전 등급 정보
 */
@Data
@NoArgsConstructor
public class HouseWithCondition {

    private House house;

    private String addressDistrict;
    private String addressCity;

    private List<SafetyGradeInfo> safetyGradeInfoList = new ArrayList<>();

    public HouseWithCondition(House house, String addressDistrict, String addressCity) {
        this.house = house;
        this.addressDistrict = addressDistrict;
        this.addressCity = addressCity;
    }

    public Double getScore() {
        return this.house.getScore();
    }

    @Data
    @AllArgsConstructor
    public static class SafetyGradeInfo {
        private PublicData publicData;
        private Integer weight;
        private Integer grade;
    }

}
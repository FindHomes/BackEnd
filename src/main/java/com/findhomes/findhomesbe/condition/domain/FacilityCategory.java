package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

@Getter
public enum FacilityCategory {
    RestaurantIndustry("음식점"), GameIndustry("피시방"), HospitalIndustry("병원");

    private final String facilityCategory;

    FacilityCategory(String facilityCategory) {
        this.facilityCategory = facilityCategory;
    }

    public static String getAllData() {
        StringBuilder result = new StringBuilder();

        // 모든 enum 값을 순회하며 houseOption 값을 추가
        for (FacilityCategory option : FacilityCategory.values()) {
            if (!result.isEmpty()) {
                result.append(", ");
            }
            result.append(option.getFacilityCategory());
        }

        return result.toString();
    }
}

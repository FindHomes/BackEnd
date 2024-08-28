package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum FacilityCategory {
    음식점("RestaurantIndustry"), 피시방("GameIndustry"), 병원("HospitalIndustry");

    private final String facilityCategory;

    FacilityCategory(String facilityCategory) {
        this.facilityCategory = facilityCategory;
    }

    public static String getAllData() {
        return Arrays.stream(FacilityCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}

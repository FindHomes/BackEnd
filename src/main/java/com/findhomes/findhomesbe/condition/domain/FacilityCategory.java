package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum FacilityCategory {
    음식점("RestaurantIndustry"), 피시방("GameIndustry"), 종합병원("HospitalIndustry"), 공연장("ConcertHallIndustry"), 노래방("KaraokeIndustry"), 동물병원("AnimalHospitalIndustry"), 약국("PharmacyIndustry"), 영화관("CinemaIndustry"), 병원("ClincIndustry"), 베이커리("Bakery"),헬스장("GymIndustry");

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

package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.repository.RestaurantIndustryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.management.openmbean.TabularType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum FacilityCategory {

    음식점("RestaurantIndustry"),
    피시방("GameIndustry"),
    종합병원("HospitalIndustry"),
    공연장("ConcertHallIndustry"),
    노래방("KaraokeIndustry"),
    동물병원("AnimalHospitalIndustry"),
    약국("PharmacyIndustry"),
    영화관("CinemaIndustry"),
    병원("ClincIndustry"),
    베이커리("Bakery"),
    헬스장("GymIndustry"),
    미용실("BeautyIndustry");

    private final String facilityCategory;
//    private final SingleFunction<FacilityCategory, List<Industry>> getIndustryFunction;

//    FacilityCategory(String facilityCategory, SingleFunction<FacilityCategory, List<Industry>> getIndustryFunction) {
//        this.facilityCategory = facilityCategory;
//        this.getIndustryFunction = getIndustryFunction;
//    }

    FacilityCategory(String facilityCategory) {
        this.facilityCategory = facilityCategory;
    }

    public static String getAllData() {
        return Arrays.stream(FacilityCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

//    public List<Industry> getIndustries(FacilityCategory facilityCategory) {
//        return getIndustryFunction.apply(facilityCategory);
//    }
//
//    @FunctionalInterface
//    public interface SingleFunction<T, R> {
//        R apply(T t);
//    }
}

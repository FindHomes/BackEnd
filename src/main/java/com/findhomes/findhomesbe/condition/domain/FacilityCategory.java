package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.entity.industry.Industry;
import com.findhomes.findhomesbe.repository.industry.*;
import lombok.Getter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 각 FacilityCategory enum 객체는 repository 빈 이름과 repository에서 데이터를 가져오는 람다식을 필드로 가짐.
 * repository를 여기서 조회하는걸 생각했는데, enum의 라이프사이클상 repository 빈 보다 먼저 생성돼서 여기서 바로 참조가 불가능함.
 * 그래서 람다식을 호출할 때 repository bean 객체를 넣어주는 방식으로 했음...
 * 이게 처음에 여기서 repository를 가져오는 걸로 생각했던거라 좀 이상할 수 있는데 수정하면 좋긴할듯... 언젠가..
 */
@Getter
public enum FacilityCategory {
    학교("SchoolIndustryRepository",
            (repository, detailName) ->
                    ((SchoolIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    동물병원("animalHospitalIndustryRepository",
            (repository, detailName) ->
                    ((AnimalHospitalIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    베이커리("bakeryIndustryRepository",
            (repository, detailName) ->
                    ((BakeryIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    목욕탕("bathhouseIndustryRepository",
            (repository, detailName) ->
                    ((BathhouseIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    미용실("beautyIndustryRepository",
            (repository, detailName) ->
                    ((BeautyIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    영화관("cinemaIndustryRepository",
            (repository, detailName) ->
                    ((CinemaIndustryRepository) repository).findByDetailName(detailName),
            3d
    ),
    병원("clinicIndustryRepository",
            (repository, detailName) ->
                    ((ClinicIndustryRepository) repository).findByDetailName(detailName),
            2d
    ),
    공연장("concertHallIndustryRepository",
            (repository, detailName) ->
                    ((ConcertHallIndustryRepository) repository).findByDetailName(detailName),
            3d
    ),
    피시방("gameIndustryRepository",
            (repository, detailName) ->
                    ((GameIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    헬스장("gymIndustryRepository",
            (repository, detailName) ->
                    ((GymIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    종합병원("hospitalIndustryRepository",
            (repository, detailName) ->
                    ((HospitalIndustryRepository) repository).findByDetailName(detailName),
            3d
    ),
    노래방("karaokeIndustryRepository",
            (repository, detailName) ->
                    ((KaraokeIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    약국("pharmacyIndustryRepository",
            (repository, detailName) ->
                    ((PharmacyIndustryRepository) repository).findByDetailName(detailName),
            1d
    ),
    음식점("restaurantIndustryRepository",
            (repository, detailName) ->
                    ((RestaurantIndustryRepository) repository).findByDetailName(detailName),
            1.5d
    );

    private final String repositoryBeanName;
    private final BiFunction<JpaRepository<? extends Industry, Integer>, String, List<? extends Industry>> getIndustryListWhenNotAllFunction;
    private final Double maxRadius;

    FacilityCategory(String repositoryBeanName,
                     BiFunction<JpaRepository<? extends Industry, Integer>, String, List<? extends Industry>> getIndustryListWhenNotAllFunction,
                     Double maxRadius) {
        this.repositoryBeanName = repositoryBeanName;
        this.getIndustryListWhenNotAllFunction = getIndustryListWhenNotAllFunction;
        this.maxRadius = maxRadius;
    }

    // detailName이 "all"이 아닐 경우 호출해서 Industry 리스트를 반환받는 함수.
    public <T extends Industry> List<Industry> getIndustryListWhenNotAll(JpaRepository<T, Integer> repository, String detailName) {
        return (List<Industry>) getIndustryListWhenNotAllFunction.apply(repository, detailName);
    }

    // 보유 데이터 목록 문자열로 반환
    public static String getAllData() {
        return Arrays.stream(FacilityCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

}

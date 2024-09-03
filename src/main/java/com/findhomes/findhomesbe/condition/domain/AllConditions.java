package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AllConditions {
    public AllConditions(ManConRequest manConRequest) {
        this.manConRequest = manConRequest;
    }

    // 매물 필수 조건
    private ManConRequest manConRequest;
    // 매물 자체 조건
    private List<HouseConditionData> houseConditionDataList = new ArrayList<>();
    // 매물 필수 옵션
    private List<HouseOption> houseOptionList = new ArrayList<>();
    // 시설 조건
    private List<FacilityConditionData> facilityConditionDataList = new ArrayList<>();
    // 공공데이터 조건
    private List<PublicConditionData> publicConditionDataList = new ArrayList<>();
    // 사용자 요청 위치 조건
    private List<UserRequestLocationData> userRequestLocationDataList = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class HouseConditionData {
        private HouseCondition houseConditionEnum;
        private Object value;

        @Override
        public String toString() {
            return "houseConditionEnum=" + houseConditionEnum +
                    ", detail='" + value + '\'';
        }
    }

    @Data
    @AllArgsConstructor
    public static class FacilityConditionData {
        private FacilityCategory facilityCategoryEnum;
        private String detailName;
        private Integer weight;

        @Override
        public String toString() {
            return "facilityCategoryEnum=" + facilityCategoryEnum +
                    ", detailName='" + detailName + '\'' +
                    ", weight=" + weight;
        }
    }

    @Data
    @AllArgsConstructor
    public static class PublicConditionData {
        private PublicData publicDataEnum;
        private Integer weight;

        @Override
        public String toString() {
            return "publicDataEnum=" + publicDataEnum +
                    ", weight=" + weight;
        }
    }

    @Data
    @AllArgsConstructor
    public static class UserRequestLocationData {
        private String locationName;
        private Double latitude;
        private Double longitude;
        private Integer weight;

        @Override
        public String toString() {
            return "locationName='" + locationName + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", weight=" + weight;
        }
    }

    @Override
    public String toString() {
        return "[manConRequest]\n" + manConRequest +
                "\n[houseConditionDataList]\n" + houseConditionDataList.stream().map(HouseConditionData::toString).collect(Collectors.joining("\n")) +
                "\n[houseOptionList]\n" + houseOptionList.stream().map(HouseOption::toString).collect(Collectors.joining("\n")) +
                "\n[facilityConditionDataList]\n" + facilityConditionDataList.stream().map(FacilityConditionData::toString).collect(Collectors.joining("\n")) +
                "\n[publicConditionDataList]\n" + publicConditionDataList.stream().map(PublicConditionData::toString).collect(Collectors.joining("\n")) +
                "\n[userRequestLocationDataList]\n" + userRequestLocationDataList.stream().map(UserRequestLocationData::toString).collect(Collectors.joining("\n"));
    }
}

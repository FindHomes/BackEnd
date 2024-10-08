package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class AllConditions {
    public AllConditions(ManConRequest manConRequest, List<String> keywords) {
        this.manConRequest = manConRequest;
        this.keywords = keywords;
    }

    // 키워드 리스트
    private List<String> keywords;

    // 매물 필수 조건
    private ManConRequest manConRequest;
    // 매물 자체 조건
    private List<HouseConditionData> houseConditionDataList = new ArrayList<>();
    // 매물 필수 옵션
    private List<HouseOptionData> houseOptionDataList = new ArrayList<>();
    // 시설 조건
    private List<FacilityConditionData> facilityConditionDataList = new ArrayList<>();
    // 공공데이터 조건
    private List<PublicConditionData> publicConditionDataList = new ArrayList<>();
    // 사용자 요청 위치 조건
    private List<UserRequestLocationData> userRequestLocationDataList = new ArrayList<>();

    public interface KeywordContains {
        String getKeyword();
    }

    @Data
    @AllArgsConstructor
    public static class HouseConditionData implements KeywordContains {
        private String keyword;
        private HouseCondition houseConditionEnum;
        private Object value;

        @Override
        public String toString() {
            return "keyword=" + keyword +
                    ", houseConditionEnum=" + houseConditionEnum +
                    ", detail='" + value + '\'';
        }
    }

    @Data
    @AllArgsConstructor
    public static class HouseOptionData implements KeywordContains {
        private String keyword;
        private HouseOption option;

        @Override
        public String toString() {
            return "keyword=" + keyword +
                    ", option='" + option.getHouseOption() + '\'';
        }
    }

    @Data
    @AllArgsConstructor
    public static class FacilityConditionData implements KeywordContains {
        private String keyword;
        private FacilityCategory facilityCategoryEnum;
        private String detailName;
        private Integer weight;
        private Double maxRadius;

        @Override
        public String toString() {
            return "keyword=" + keyword +
                    ", facilityCategoryEnum=" + facilityCategoryEnum +
                    ", detailName='" + detailName + '\'' +
                    ", weight=" + weight + '\'' +
                    ", maxRadius=" + maxRadius;
        }
    }

    @Data
    @AllArgsConstructor
    public static class PublicConditionData implements KeywordContains {
        private String keyword;
        private PublicData publicDataEnum;
        private Integer weight;

        @Override
        public String toString() {
            return "keyword=" + keyword +
                    ", publicDataEnum=" + publicDataEnum +
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
                "\n[houseOptionList]\n" + houseOptionDataList.stream().map(HouseOptionData::toString).collect(Collectors.joining("\n")) +
                "\n[facilityConditionDataList]\n" + facilityConditionDataList.stream().map(FacilityConditionData::toString).collect(Collectors.joining("\n")) +
                "\n[publicConditionDataList]\n" + publicConditionDataList.stream().map(PublicConditionData::toString).collect(Collectors.joining("\n")) +
                "\n[userRequestLocationDataList]\n" + userRequestLocationDataList.stream().map(UserRequestLocationData::toString).collect(Collectors.joining("\n"));
    }
}

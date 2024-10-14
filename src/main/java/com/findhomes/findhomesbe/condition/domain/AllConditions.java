package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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
    @NoArgsConstructor
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

    public String summarize() {
        String result = "";

        if (!houseConditionDataList.isEmpty()) {
            result += "매물 조건: " + houseConditionDataList.stream().map(e -> e.getHouseConditionEnum().name()).collect(Collectors.joining(", ")) + "\n";
        }
        if (!houseOptionDataList.isEmpty()) {
            result += "매물 옵션: " + houseOptionDataList.stream().map(e -> e.getOption().name()).collect(Collectors.joining(", ")) + "\n";
        }
        if (!publicConditionDataList.isEmpty()) {
            result += "공공 데이터" + publicConditionDataList.stream().map(e -> e.getPublicDataEnum().name()).collect(Collectors.joining(", ")) + "\n";
        }
        if (!facilityConditionDataList.isEmpty()) {
            result += "시설 데이터: " + facilityConditionDataList.stream().map(e -> e.getFacilityCategoryEnum().name()).collect(Collectors.joining(", ")) + "\n";
        }
        if (!userRequestLocationDataList.isEmpty()) {
            result += "기타: " + userRequestLocationDataList.stream().map(e -> e.getLocationName()).collect(Collectors.joining(", "));
        }

        return result.trim();
    }

    public static AllConditions getExampleAllConditions() {
        List<String> keywords = List.of("아이", "햄버거");
        // Ws 객체 초기화
        ManConRequest.Prices.Ws ws = new ManConRequest.Prices.Ws();
        ws.setDeposit(10000);
        ws.setRent(5000);
        // Prices 객체 초기화
        ManConRequest.Prices prices = new ManConRequest.Prices();
        prices.setMm(130000);
        prices.setJs(100000);
        prices.setWs(ws);
        // Region 객체 초기화
        ManConRequest.Region region = new ManConRequest.Region();
        region.setCity("서울특별시");
        region.setDistrict("광진구");
        // ManConRequest 객체 초기화
        ManConRequest manConRequest = new ManConRequest();
        manConRequest.setHousingTypes(Arrays.asList("원룸", "아파트"));
        manConRequest.setPrices(prices);
        manConRequest.setRegion(region);
        // HouseConditionData 초기화
        AllConditions.HouseConditionData houseConditionData = new AllConditions.HouseConditionData(
                "아이",
                HouseCondition.크기,  // 가정된 enum 값
                "30"
        );
        // PublicConditionData 초기화
        AllConditions.PublicConditionData publicConditionData = new AllConditions.PublicConditionData(
                "아이",
                PublicData.범죄율,  // 가정된 enum 값
                7
        );
        // AllConditions 초기화
        AllConditions allConditions = new AllConditions(manConRequest, keywords);
        allConditions.getHouseConditionDataList().add(houseConditionData);
        allConditions.getPublicConditionDataList().add(publicConditionData);

        return allConditions;
    }
}

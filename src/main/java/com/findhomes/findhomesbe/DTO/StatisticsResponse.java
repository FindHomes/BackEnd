package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.FacilityCategory;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.entity.House;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class StatisticsResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private List<KeywordInfo> result;

    public static StatisticsResponse of(List<HouseWithCondition> houses, AllConditions allConditions, Boolean success, Integer code, String message) {
        StatisticsResponse response = new StatisticsResponse();
        response.setSuccess(success);
        response.setCode(code);
        response.setMessage(message);
        List<KeywordInfo> curResult = new ArrayList<>();
        // 각 키워드 별 정보 객체를 생성
        for (String keyword : allConditions.getKeywords()) {
            KeywordInfo keywordInfo = new KeywordInfo(keyword);

            // 매물 자체 조건
            for (AllConditions.HouseConditionData houseConditionData : allConditions.getHouseConditionDataList()) {
                if (houseConditionData.getKeyword().equals(keyword)) {
                    keywordInfo.getHouseConditions().add(houseConditionData.getHouseConditionEnum().name());
                }
            }
            // 매물 옵션
            for (AllConditions.HouseOptionData houseOptionData : allConditions.getHouseOptionDataList()) {
                if (houseOptionData.getKeyword().equals(keyword)) {
                    keywordInfo.getHouseOptions().add(houseOptionData.getOption().name());
                }
            }
            // 시설 데이터
            for (AllConditions.FacilityConditionData facilityConditionData : allConditions.getFacilityConditionDataList()) {
                if (!facilityConditionData.getKeyword().equals(keyword)) {
                    continue;
                }
                FacilityCategory facilityCategory = facilityConditionData.getFacilityCategoryEnum();
                KeywordInfo.DataAndInfo dataAndInfo = new KeywordInfo.DataAndInfo(
                        facilityCategory.getMaxRadius() + "km 이내 " + facilityCategory.name() + "-" + facilityConditionData.getDetailName()
                );

                for (HouseWithCondition houseWithCondition : houses) {
                    for (HouseWithCondition.FacilityInfo facilityInfo : houseWithCondition.getFacilityInfoList()) {
                        if (facilityInfo.getFacilityConditionData().equals(facilityConditionData)) {
                            House house = houseWithCondition.getHouse();
                            List<KeywordInfo.Value> values = new ArrayList<>();
                            values.add(new KeywordInfo.Value("개수", facilityInfo.getCount().doubleValue()));
                            values.add(new KeywordInfo.Value("평균 거리(km)", Math.round(facilityInfo.getDistanceSum() * 100 / facilityInfo.getCount()) / 100d));
                            KeywordInfo.HouseAndValue houseAndValue = new KeywordInfo.HouseAndValue(
                                    house.getHouseId(), house.getRanking(), values);
                            dataAndInfo.getHouseAndValues().add(houseAndValue);
                        }
                    }
                }

                keywordInfo.getFacilityAndInfos().add(dataAndInfo);
            }
            // 공공 데이터
            for (AllConditions.PublicConditionData publicConditionData : allConditions.getPublicConditionDataList()) {
                if (!publicConditionData.getKeyword().equals(keyword)) {
                    continue;
                }
                KeywordInfo.DataAndInfo dataAndInfo = new KeywordInfo.DataAndInfo(publicConditionData.getPublicDataEnum().name() + " (1~5 등급)");

                for (HouseWithCondition houseWithCondition : houses) {
                    for (HouseWithCondition.SafetyGradeInfo safetyGradeInfo : houseWithCondition.getSafetyGradeInfoList()) {
                        if (safetyGradeInfo.getKeyword().equals(keyword) && safetyGradeInfo.getPublicData().equals(publicConditionData.getPublicDataEnum())) {
                            House house = houseWithCondition.getHouse();
                            List<KeywordInfo.Value> values = new ArrayList<>();
                            values.add(new KeywordInfo.Value("등급", safetyGradeInfo.getGrade().doubleValue()));
                            KeywordInfo.HouseAndValue houseAndValue = new KeywordInfo.HouseAndValue(house.getHouseId(), house.getRanking(), values);
                            dataAndInfo.getHouseAndValues().add(houseAndValue);
                        }
                    }
                }

                keywordInfo.getPublicDataAndInfos().add(dataAndInfo);
            }

            curResult.add(keywordInfo);
        }
        response.setResult(curResult);
        return response;
    }

    @Data
    public static class KeywordInfo {
        private String keyword;
        private List<String> houseConditions = new ArrayList<>();
        private List<String> houseOptions = new ArrayList<>();
        private List<DataAndInfo> facilityAndInfos = new ArrayList<>();
        private List<DataAndInfo> publicDataAndInfos = new ArrayList<>();

        public KeywordInfo(String keyword) {
            this.keyword = keyword;
        }

        @Data
        @AllArgsConstructor
        public static class HouseAndValue {
            private Integer houseId;
            private Integer ranking;
            private List<Value> values;
        }

        @Data
        public static class DataAndInfo {
            public DataAndInfo(String dataName) {
                this.dataName = dataName;
            }

            private String dataName;
            private List<HouseAndValue> houseAndValues = new ArrayList<>();
        }

        @Data
        @AllArgsConstructor
        public static class Value {
            private String name;
            private Double value;
        }

    }

}

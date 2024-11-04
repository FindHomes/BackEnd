package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetailSearchResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private HouseAndStat result;

    public HouseDetailSearchResponse(HouseWithCondition houseWithCondition, Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = new HouseAndStat(
                new ResponseHouse(houseWithCondition.getHouse(), houseWithCondition.isFavorite()),
                houseWithCondition.getSafetyGradeInfoList(),
                houseWithCondition.getFacilityInfoList()
        );
    }

    @Data
    public static class HouseAndStat {
        public HouseAndStat(ResponseHouse responseHouse, List<HouseWithCondition.SafetyGradeInfo> safetyGradeInfoList, List<HouseWithCondition.FacilityInfo> facilityInfoList) {
            this.responseHouse = responseHouse;

            for (HouseWithCondition.SafetyGradeInfo info : safetyGradeInfoList) {
                String newStat = info.getPublicData().name() + ": " + info.getGrade() + "등급";
                stats.add(newStat);
            }
            for (HouseWithCondition.FacilityInfo info : facilityInfoList) {
                String facilityDetailName = info.getFacilityConditionData().getDetailName();
                String newStat = info.getFacilityConditionData().getMaxRadius() + "km 이내-" +
                        info.getFacilityConditionData().getFacilityCategoryEnum().name() + "-" +
                        (facilityDetailName.equalsIgnoreCase("all") ? "전체" : facilityDetailName) + ": " +
                        info.getCount() + "개, 평균 거리 " +
                        info.getAvgDistance() + "km";
                stats.add(newStat);
            }
        }

        private ResponseHouse responseHouse;
        private List<String> stats = new ArrayList<>();
    }

}

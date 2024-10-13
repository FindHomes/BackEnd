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
            this.safetyGradeInfoList = safetyGradeInfoList;
            this.facilityInfoList = facilityInfoList;
        }

        private ResponseHouse responseHouse;
        private List<HouseWithCondition.SafetyGradeInfo> safetyGradeInfoList;
        private List<HouseWithCondition.FacilityInfo> facilityInfoList;
    }

}

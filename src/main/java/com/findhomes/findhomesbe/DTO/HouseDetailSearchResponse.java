package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetailSearchResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private ResponseHouse result;

    public HouseDetailSearchResponse(HouseWithCondition houseWithCondition, Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = new ResponseHouse(houseWithCondition.getHouse(), houseWithCondition.isFavorite());
    }
}

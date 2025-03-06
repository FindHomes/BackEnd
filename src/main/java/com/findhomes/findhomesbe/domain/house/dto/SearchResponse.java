package com.findhomes.findhomesbe.domain.house.dto;

import com.findhomes.findhomesbe.domain.condition.domain.HouseWithCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {

    private Boolean success;
    private Integer code;
    private String message;
    private List<ResponseHouse> result;

    public SearchResponse(List<HouseWithCondition> houses, Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = houses.stream()
                .map(houseWithCondition ->
                        new ResponseHouse(houseWithCondition.getHouse(), houseWithCondition.isFavorite()))
                .collect(Collectors.toList());
    }
}

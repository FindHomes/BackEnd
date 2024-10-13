package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetailResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private ResponseHouse result;

    public HouseDetailResponse(House house, boolean isFavorite, Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = new ResponseHouse(house,isFavorite);
    }

    public HouseDetailResponse(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}

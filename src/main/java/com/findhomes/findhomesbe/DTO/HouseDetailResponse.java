package com.findhomes.findhomesbe.DTO;

import com.findhomes.findhomesbe.entity.House;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetailResponse {
    private Boolean success;
    private Integer code;
    private String message;
    private House result;
}

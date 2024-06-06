package com.findhomes.findhomesbe.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class House {
    private Integer houseId;
    private String priceType;
    private Integer price;
    private Integer priceForWS;
    private String housingType;
    private Float size;
    private Integer roomNum;
    private Integer washroomNum;
    private String address;
    private Double x;
    private Double y;
    private transient Double score; // 직렬화에서 제외됨
}

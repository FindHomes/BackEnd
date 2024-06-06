package com.findhomes.findhomesbe.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class House {
    private Integer houseId; // 8자리 숫자
    private String priceType; // mm, js, ws
    private Integer price; // 매매가, 전세가, 보증금
    private Integer priceForWS; // priceType이 ws일 경우 월세. mm, js일 경우는 0
    private String housingType; // ONE, TWO, THREE(방 3개 이상)
    private Float size; // m^2 집크기
    private Integer roomNum; // 방 개수
    private Integer washroomNum; // 화장실 개수
    private String address; // 한글 주소
    private Double x; // 경도
    private Double y; // 위도
}

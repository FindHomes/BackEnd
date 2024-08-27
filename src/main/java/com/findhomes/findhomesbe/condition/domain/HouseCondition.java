package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

@Getter
public enum HouseCondition {
    maintenanceFee("관리비"),isMultiLayer("복층"), isSeparateType("분리형"),
    floor("층수"), size("크기"), roomNum("방 수"), washroomNum("화장실 수"),
    direction("방향"), completionDate("완공일"), houseOption("옵션");

    private final String houseContidion;

    HouseCondition(String houseContidion) {
        this.houseContidion = houseContidion;
    }

    public static String getAllData() {
        StringBuilder result = new StringBuilder();

        // 모든 enum 값을 순회하며 houseOption 값을 추가
        for (HouseCondition option : HouseCondition.values()) {
            if (!result.isEmpty()) {
                result.append(", ");
            }
            result.append(option.getHouseContidion());
        }

        return result.toString();
    }
}

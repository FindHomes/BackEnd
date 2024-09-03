package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum HouseDirection {
    북, 북동,
    동, 남동,
    남, 남서,
    서, 북서;

    public static String getAllData() {
        return Arrays.stream(HouseDirection.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    // 예외 발생 조심 알아서 처리해야됨. 여기서 처리 ㄴㄴ
    public static String getHouseDirection(String direction) {
        return valueOf(direction).name();
    }
}

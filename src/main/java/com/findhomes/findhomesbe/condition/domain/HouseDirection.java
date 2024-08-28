package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum HouseDirection {
    북("북"), 북동("북동"),
    동("동"), 남동("남동"),
    남("남"), 남서("남서"),
    서("서"), 북서("북서");

    private final String houseDirection;

    HouseDirection(String houseDirection) {
        this.houseDirection = houseDirection;
    }

    public static String getAllData() {
        return Arrays.stream(HouseDirection.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}

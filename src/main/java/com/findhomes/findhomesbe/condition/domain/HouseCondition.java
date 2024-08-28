package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum HouseCondition {
    관리비,복층, 분리형,
    층수, 크기, 방_수, 화장실_수,
    방향, 완공일, 옵션;

    public static String getAllData() {
        return Arrays.stream(HouseCondition.values())
                .map(Enum::name)  // Enum 상수의 이름을 가져옴
                .collect(Collectors.joining(", "));  // 이름들을 콤마로 구분하여 연결
    }
}

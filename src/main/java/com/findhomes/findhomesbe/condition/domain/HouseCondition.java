package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.condition.etc.ConversionUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HouseCondition {
    관리비(0, ConversionUtils::toInteger),
    복층(1, ConversionUtils::toBoolean),
    분리형(2, ConversionUtils::toBoolean),
    층수(3, ConversionUtils::toInteger),
    크기(4, ConversionUtils::toInteger),
    방_수(5, ConversionUtils::toInteger),
    화장실_수(6, ConversionUtils::toInteger),
    방향(7, ConversionUtils::toDirectionString),
    완공일(8, ConversionUtils::toLocalDate);

    @Getter
    private final int index;
    private final Function<String, ?> parser;

    HouseCondition(int index, Function<String, ?> parser) {
        this.index = index;
        this.parser = parser;
    }

    public Object parse(String value) {
        return parser.apply(value);
    }

    public static String getAllData() {
        return Arrays.stream(HouseCondition.values())
                .map(Enum::name)  // Enum 상수의 이름을 가져옴
                .collect(Collectors.joining(", "));  // 이름들을 콤마로 구분하여 연결
    }
}

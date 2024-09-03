package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum PublicData {
    // gpt한테 제공할 이름 & 테이블에서의 필드명
    교통사고율, 화재율,
    범죄율, 생활안전,
    자살율, 감염병율;

    public static String getAllData() {
        return Arrays.stream(PublicData.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}

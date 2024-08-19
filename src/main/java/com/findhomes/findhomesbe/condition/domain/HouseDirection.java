package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum HouseDirection {
    북("북", "north", "n"),
    북동("북동", "northeast", "ne"),
    동("동", "east", "e"),
    남동("남동", "southeast", "se"),
    남("남", "south", "s"),
    남서("남서", "southwest", "sw"),
    서("서", "west", "w"),
    북서("북서", "northwest", "nw");

    // 필드: 한글, 영어(Full), 영어(약어)
    private final String koreanName;
    private final String englishFullName;
    private final String englishAbbreviation;

    // 매핑 테이블을 사용하여 빠른 변환을 지원
    private static final Map<String, HouseDirection> lookupTable = new HashMap<>();

    // Enum 생성자
    HouseDirection(String koreanName, String englishFullName, String englishAbbreviation) {
        this.koreanName = koreanName;
        this.englishFullName = englishFullName;
        this.englishAbbreviation = englishAbbreviation;
    }

    // Enum이 로드될 때 각 이름을 맵핑 테이블에 추가
    static {
        for (HouseDirection direction : HouseDirection.values()) {
            lookupTable.put(direction.koreanName.toLowerCase(), direction);
            lookupTable.put(direction.englishFullName.toLowerCase(), direction);
            lookupTable.put(direction.englishAbbreviation.toLowerCase(), direction);
        }
    }

    // 특정 문자열을 한글 방향으로 변환하는 메서드
    public static HouseDirection convertToKorean(String input) {
        return lookupTable.get(input.toLowerCase());
    }

    public static String getAllData() {
        StringBuilder result = new StringBuilder();

        // 모든 enum 값을 순회하며 houseOption 값을 추가
        for (HouseDirection option : HouseDirection.values()) {
            if (!result.isEmpty()) {
                result.append(", ");
            }
            result.append(option.getKoreanName());
        }

        return result.toString();
    }
}

package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

@Getter
public enum PublicData {
    교통사고율("교통사고율"),
    화재율("화재율"),
    범죄율("범죄율"),
    생활안전("생활안전"),
    자살율("자살율"),
    감염병율("감염병율");

    private final String publicData;

    PublicData(String publicData) {
        this.publicData = publicData;
    }

    public static String getAllData() {
        StringBuilder result = new StringBuilder();

        // 모든 enum 값을 순회하며 houseOption 값을 추가
        for (PublicData option : PublicData.values()) {
            if (!result.isEmpty()) {
                result.append(", ");
            }
            result.append(option.getPublicData());
        }

        return result.toString();
    }
}

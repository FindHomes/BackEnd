package com.findhomes.findhomesbe.calculate.data;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum SafetyEnum {
    교통사고율("trafficAccidents"),
    화재율("fire"),
    범죄율("crime"),
    생활안전("publicSafety"),
    자살율("suicide"),
    감염병율("infectiousDiseases");

    private final String value;

    SafetyEnum(String value) {
        this.value = value;
    }
}

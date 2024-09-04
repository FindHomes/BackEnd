package com.findhomes.findhomesbe.condition.domain;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum PublicData {
    // gpt한테 제공할 이름 & 테이블에서의 필드명
    교통사고율((grade, weight) -> (3d - grade) * weight),
    화재율((grade, weight) -> (3d - grade) * weight),
    범죄율((grade, weight) -> (3d - grade) * weight),
    생활안전((grade, weight) -> (3d - grade) * weight),
    자살율((grade, weight) -> (3d - grade) * weight),
    감염병율((grade, weight) -> (3d - grade) * weight);

    private final BiFunction<Integer, Integer, Double> scoreFunction;

    PublicData(BiFunction<Integer, Integer, Double> scoreFunction) {
        this.scoreFunction = scoreFunction;
    }

    public Double calculateScore(Integer grade, Integer weight) {
        return scoreFunction.apply(grade, weight);
    }

    public static String getAllData() {
        return Arrays.stream(PublicData.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

}

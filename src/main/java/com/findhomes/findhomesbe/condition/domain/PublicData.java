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
    교통사고율((grade, ratio) -> (5d - grade) * 25 * ratio),
    화재율((grade, ratio) -> (5d - grade) * 25 * ratio),
    범죄율((grade, ratio) -> (5d - grade) * 25 * ratio),
    생활안전((grade, ratio) -> (5d - grade) * 25 * ratio),
    자살율((grade, ratio) -> (5d - grade) * 25 * ratio),
    감염병율((grade, ratio) -> (5d - grade) * 25 * ratio);

    private final BiFunction<Integer, Double, Double> scoreFunction;

    PublicData(BiFunction<Integer, Double, Double> scoreFunction) {
        this.scoreFunction = scoreFunction;
    }

    public Double calculateScore(Integer grade, Double ratio) {
        return scoreFunction.apply(grade, ratio);
    }

    public static String getAllData() {
        return Arrays.stream(PublicData.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

}

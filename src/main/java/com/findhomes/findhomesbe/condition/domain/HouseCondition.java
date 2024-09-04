package com.findhomes.findhomesbe.condition.domain;

import com.findhomes.findhomesbe.condition.etc.ConversionUtils;
import com.findhomes.findhomesbe.entity.House;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HouseCondition {
    관리비(0, "maintenanceFee", ConversionUtils::toInteger,
            (cb, root, value) -> cb.or(
                    cb.isNull(root.get("maintenanceFee")),
                    cb.lessThanOrEqualTo(root.get("maintenanceFee"), (Integer) value)
            )),
    복층(1, "isMultiLayer", ConversionUtils::toBoolean,
            (cb, root, value) -> cb.equal(root.get("isMultiLayer"), value)),
    분리형(2, "isSeparateType", ConversionUtils::toBoolean,
            (cb, root, value) -> cb.equal(root.get("isSeparateType"), value)),
    층수(3, "floor", ConversionUtils::toInteger,
            // TODO: 층수 지금 람다식이 임시로 무조건 통과되게 되어있는데 이거 수정해야 됨.
            (cb, root, value) -> cb.conjunction()),
    크기(4, "size", ConversionUtils::toDouble,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("size"), (Double) value)),
    방_수(5, "roomNum", ConversionUtils::toInteger,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("roomNum"), (Integer) value)),
    화장실_수(6, "washroomNum", ConversionUtils::toInteger,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("washroomNum"), (Integer) value)),
    방향(7, "direction", ConversionUtils::toDirectionString,
            (cb, root, value) -> cb.equal(root.get("direction"), (String) value)),
    완공일(8, "completionDate", ConversionUtils::toLocalDate,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("completionDate"), (LocalDate) value));

    @Getter
    private final int index;
    private final String fieldName;
    private final Function<String, ?> parser;
    private final TriParamFunction<CriteriaBuilder, Root<House>, Object, Predicate> conditionFunction;

    HouseCondition(int index, String fieldName, Function<String, ?> parser, TriParamFunction<CriteriaBuilder, Root<House>, Object, Predicate> conditionFunction) {
        this.index = index;
        this.fieldName = fieldName;
        this.parser = parser;
        this.conditionFunction = conditionFunction;
    }

    public Object parse(String value) {
        return parser.apply(value);
    }

    public static String getAllData() {
        return Arrays.stream(HouseCondition.values())
                .map(Enum::name)  // Enum 상수의 이름을 가져옴
                .collect(Collectors.joining(", "));  // 이름들을 콤마로 구분하여 연결
    }

    public Predicate buildPredicate(CriteriaBuilder cb, Root<House> root, Object value) {
        return conditionFunction.apply(cb, root, value);
    }

    @FunctionalInterface
    public interface TriParamFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}

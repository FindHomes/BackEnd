package com.findhomes.findhomesbe.domain.condition.domain;

import com.findhomes.findhomesbe.domain.condition.etc.ConversionUtils;
import com.findhomes.findhomesbe.domain.house.domain.House;
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
            ),
            (value) -> "maintenance_fee <= " + value),
    복층(1, "isMultiLayer", ConversionUtils::toBoolean,
            (cb, root, value) -> cb.equal(root.get("isMultiLayer"), value),
            (value) -> "is_multi_layer = " + value),
    분리형(2, "is_separate_type", ConversionUtils::toBoolean,
            (cb, root, value) -> cb.equal(root.get("isSeparateType"), value),
            (value) -> "is_separate_type = " + value),
    층수(3, "floor", ConversionUtils::toInteger,
            // TODO: 층수 지금 람다식이 임시로 무조건 통과되게 되어있는데 이거 수정해야 됨.
            (cb, root, value) -> cb.conjunction(),
            (value) -> "1 = 1"),
    크기(4, "size", ConversionUtils::toDouble,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("size"), (Double) value),
            (value) -> "size >= " + value),
    방_수(5, "roomNum", ConversionUtils::toInteger,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("roomNum"), (Integer) value),
            (value) -> "room_num >= " + value),
    화장실_수(6, "washroomNum", ConversionUtils::toInteger,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("washroomNum"), (Integer) value),
            (value) -> "washroom_num >= " + value),
    방향(7, "direction", ConversionUtils::toDirectionString,
            (cb, root, value) -> cb.equal(root.get("direction"), (String) value),
            (value) -> "direction = " + value),
    완공일(8, "completionDate", ConversionUtils::toLocalDate,
            (cb, root, value) -> cb.greaterThanOrEqualTo(root.get("completionDate"), (LocalDate) value),
            (value) -> "completion_date >= " + value),;

    @Getter
    private final int index;
    private final String fieldName;
    private final Function<String, ?> parser;
    private final TriParamFunction<CriteriaBuilder, Root<House>, Object, Predicate> conditionFunction;
    public final Function<Object, String> jdbcTemplateFunction;

    HouseCondition(int index, String fieldName, Function<String, ?> parser,
                   TriParamFunction<CriteriaBuilder, Root<House>, Object, Predicate> conditionFunction,
                   Function<Object, String> jdbcTemplateFunction
    ) {
        this.index = index;
        this.fieldName = fieldName;
        this.parser = parser;
        this.conditionFunction = conditionFunction;
        this.jdbcTemplateFunction = jdbcTemplateFunction;
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

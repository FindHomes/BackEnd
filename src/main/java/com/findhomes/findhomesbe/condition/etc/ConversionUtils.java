package com.findhomes.findhomesbe.condition.etc;

import com.findhomes.findhomesbe.condition.domain.HouseDirection;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConversionUtils {

    public static Integer toInteger(String value) {
        String intStr = value.replaceAll("[^0-9]", "");
        return intStr.isEmpty() ? null : Integer.parseInt(intStr);
    }

    public static Double toDouble(String value) {
        // 숫자와 소수점(.)만 남기고 나머지 제거
        String doubleStr = value.replaceAll("[^0-9.]", "");

        // 소수점이 두 개 이상 있는 경우
        if (doubleStr.chars().filter(ch -> ch == '.').count() > 1) {
            return 0d;
        }

        return doubleStr.isEmpty() ? null : Double.parseDouble(doubleStr);
    }

    public static Boolean toBoolean(String value) {
        boolean isContainsTrue = value.toLowerCase().contains("true");
        boolean isContainsFalse = value.toLowerCase().contains("false");
        if (isContainsTrue && isContainsFalse) {
            return null;
        } else if (isContainsTrue) {
            return true;
        } else if (isContainsFalse) {
            return false;
        } else {
            return null;
        }
    }

    public static LocalDate toLocalDate(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(value, formatter);
    }

    public static String toDirectionString(String value) {
        return HouseDirection.getHouseDirection(value);
    }
}

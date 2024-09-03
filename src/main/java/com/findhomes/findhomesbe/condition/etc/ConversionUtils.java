package com.findhomes.findhomesbe.condition.etc;

import com.findhomes.findhomesbe.condition.domain.HouseDirection;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConversionUtils {

    public static Integer toInteger(String value) {
        String intStr = value.replaceAll("[^0-9]", "");
        return intStr.isEmpty() ? null : Integer.parseInt(intStr);
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

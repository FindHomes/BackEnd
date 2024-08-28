package com.findhomes.findhomesbe.condition.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum HouseOption {
    화재경보기("화재경보기"), 붙박이장("붙박이장"), 인덕션("인덕션"),
    싱크대("싱크대"), 세탁기("세탁기"), 냉장고("냉장고"),
    신발장("신발장"), 식탁("식탁"), 책상("책상"),
    옷장("옷장"), 현관보안("현관보안"), CCTV("CCTV"),
    경비원("경비원"), 비디오폰("비디오폰"), 전자레인지("전자레인지"),
    가스레인지("가스레인지"), 샤워부스("샤워부스"), 방범창("방범창"),
    인터폰("인터폰"), 카드키("카드키"),
    벽걸이형_에어컨("벽걸이형"), 스탠드형_에어컨("스탠드형");

    private final String houseOption;

    HouseOption(String houseOption) {
        this.houseOption = houseOption;
    }

    public static String getAllData() {
        return Arrays.stream(HouseOption.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}

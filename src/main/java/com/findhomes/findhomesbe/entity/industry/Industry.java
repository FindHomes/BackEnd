package com.findhomes.findhomesbe.entity.industry;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Industry {
    // 공통 필드 정의
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String category;
    private String roadAddress;
    private String placeTags;
}

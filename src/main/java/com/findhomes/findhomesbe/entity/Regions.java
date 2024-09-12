package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;  // JTS Geometry 타입을 사용합니다.

@Entity
@Table(name = "regions_tbl")  // 데이터베이스 테이블 이름과 매핑
public class Regions {

    @Id  // 기본 키 필드를 지정합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가 설정
    @Column(name = "id")  // 데이터베이스 컬럼 이름과 매핑
    private int id;

    @Column(name = "sig_cd", nullable = false)  // nullable=false로 NOT NULL 설정
    private String sigCd;

    @Column(name = "sig_eng_nm", nullable = false)
    private String sigEngNm;

    @Column(name = "sig_kor_nm", nullable = false)
    private String sigKorNm;

    @Column(name = "boundary", columnDefinition = "geometry")  // 공간 데이터 타입 지정
    private Geometry boundary;
}

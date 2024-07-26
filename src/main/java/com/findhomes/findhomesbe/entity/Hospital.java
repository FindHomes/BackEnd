package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="hospital_industry_tbl")
public class Hospital implements Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hospitalIndustryId;
    private String placeName;
    private String roadAddress;
    private Integer zipcode;
    private String category;
    private Double x;
    private Double y;
    private String major;
    private String placeTags;
}
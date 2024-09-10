package com.findhomes.findhomesbe.entity.industry;

import jakarta.persistence.*;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hospital_industry_tbl")
public class HospitalIndustry extends Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hospitalIndustryId;

    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String major;
    private String placeTags;

}

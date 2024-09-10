package com.findhomes.findhomesbe.entity.industry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="clinic_industry_tbl")
public class ClinicIndustry extends Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer clinicIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String major;
    private String placeTags;

}

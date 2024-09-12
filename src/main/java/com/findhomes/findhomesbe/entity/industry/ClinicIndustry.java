package com.findhomes.findhomesbe.entity.industry;

import com.findhomes.findhomesbe.entity.Industry;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="clinic_industry_tbl")
public class ClinicIndustry implements Industry {

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

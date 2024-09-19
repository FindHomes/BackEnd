package com.findhomes.findhomesbe.entity.industry;

import com.findhomes.findhomesbe.entity.Industry;
import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="hospital_industry_tbl")
public class HospitalIndustry implements Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hospitalIndustryId;
    private String placeName;
    private String roadAddress;
    private Integer zipcode;
    private String category;
    private Double latitude;
    private Double longitude;
    private String major;
    private String placeTags;
    private Point coordinate;

}
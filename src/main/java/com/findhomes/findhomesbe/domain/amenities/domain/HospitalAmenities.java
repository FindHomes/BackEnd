package com.findhomes.findhomesbe.domain.amenities.domain;

import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="hospital_industry_tbl")
public class HospitalAmenities implements Amenities {
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
    private Point coordinate;

}
package com.findhomes.findhomesbe.domain.amenities.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.geolatte.geom.Point;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="beauty_industry_tbl")
public class BeautyAmenities implements Amenities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer beautyIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String placeTags;
    private Point coordinate;
}

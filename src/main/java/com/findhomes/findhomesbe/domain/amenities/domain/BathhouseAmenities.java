package com.findhomes.findhomesbe.domain.amenities.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geolatte.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="bathhouse_industry_tbl")
public class BathhouseAmenities implements Amenities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bathhouseIndustryId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String category;
    private String roadAddress;
    private String placeTags;
    private Point coordinate;

}

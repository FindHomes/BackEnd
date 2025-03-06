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
@Table(name="cinema_industry_tbl")
public class CinemaAmenities implements Amenities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cinemaIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String placeTags;
    private Point coordinate;

}

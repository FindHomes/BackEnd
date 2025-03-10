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
@Table(name="game_industry_tbl")
public class GameAmenities implements Amenities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gameIndustryId;
    private String placeName;
    private String roadAddress;
    private Double latitude;
    private Double longitude;
    private String placeTags;
    private Point coordinate;

}

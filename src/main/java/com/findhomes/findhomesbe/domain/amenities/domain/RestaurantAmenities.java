package com.findhomes.findhomesbe.domain.amenities.domain;


import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="backup_restaurant_tbl")
public class RestaurantAmenities implements Amenities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer restaurantIndustryId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String category;
    private String roadAddress;
    private String placeTags;
    private Point coordinate;
}

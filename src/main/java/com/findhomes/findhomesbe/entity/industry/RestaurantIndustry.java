package com.findhomes.findhomesbe.entity.industry;


import com.findhomes.findhomesbe.entity.Industry;
import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="backup_restaurant_tbl")
public class RestaurantIndustry implements Industry {
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

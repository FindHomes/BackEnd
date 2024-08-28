package com.findhomes.findhomesbe.entity;


import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="restaurant_industry_tbl")
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
}

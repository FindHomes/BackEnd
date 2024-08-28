package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="cinema_industry_tbl")
public class CinemaIndustry implements Industry{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cinemaIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String placeTags;

}

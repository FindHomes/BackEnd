package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="game_industry_tbl")
public class GameIndustry implements Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gameIndustryId;
    private String placeName;
    private String roadAddress;
    private Double x;
    private Double y;
    private Double tempX;
    private Double tempY;
    private String placeTags;
}

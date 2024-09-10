package com.findhomes.findhomesbe.entity.industry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="game_industry_tbl")
public class GameIndustry extends Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gameIndustryId;
    private String placeName;
    private String roadAddress;
    private Double latitude;
    private Double longitude;
    private String placeTags;
}

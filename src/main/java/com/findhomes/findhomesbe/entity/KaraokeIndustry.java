package com.findhomes.findhomesbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="karaoke_industry_tbl")
public class KaraokeIndustry implements Industry{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer karaokeIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String placeTags;

}

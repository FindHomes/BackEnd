package com.findhomes.findhomesbe.entity.industry;

import com.findhomes.findhomesbe.entity.Industry;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="beauty_industry_tbl")
public class BeautyIndustry implements Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer beautyIndustryId;
    private String placeName;
    private String roadAddress;
    private String category;
    private Double latitude;
    private Double longitude;
    private String placeTags;
}

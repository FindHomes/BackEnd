package com.findhomes.findhomesbe.entity.industry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="animal_hospital_tbl")
public class AnimalHospitalIndustry extends Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer animalHospitalIndustryId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String category;
    private String roadAddress;
    private String placeTags;
}
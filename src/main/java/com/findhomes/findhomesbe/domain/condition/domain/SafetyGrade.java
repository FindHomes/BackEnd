package com.findhomes.findhomesbe.domain.condition.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "safety_grade_tbl")
public class SafetyGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer safetyGradeId;
    String district;
    String city;
    Integer trafficAccidents;
    Integer fire;
    Integer crime;
    Integer publicSafety;
    Integer suicide;
    Integer infectiousDiseases;
}

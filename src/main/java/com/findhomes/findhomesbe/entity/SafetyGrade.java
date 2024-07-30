package com.findhomes.findhomesbe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SafetyGrade {
    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer safe_rank_id;
    String district;
    String city;
    int traffic_accidents;
    int fire;
    int crime;
    int public_safety;
    int suicide;
    int infectious_diseases;
}

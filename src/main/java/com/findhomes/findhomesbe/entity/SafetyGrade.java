package com.findhomes.findhomesbe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class SafetyGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int safe_rank_id;
    String district;
    String city;
    int traffic_accidents;
    int fire;
    int crime;
    int public_safety;
    int suicide;
    int infectious_diseases;
}

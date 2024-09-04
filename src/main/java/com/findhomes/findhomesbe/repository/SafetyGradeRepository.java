package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.SafetyGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SafetyGradeRepository extends JpaRepository<SafetyGrade, Integer> {
    Optional<SafetyGrade> findByDistrictAndCity(String district, String city);
}

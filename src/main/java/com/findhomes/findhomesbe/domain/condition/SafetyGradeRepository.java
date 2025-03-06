package com.findhomes.findhomesbe.domain.condition;

import com.findhomes.findhomesbe.domain.condition.domain.SafetyGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SafetyGradeRepository extends JpaRepository<SafetyGrade, Integer> {
    Optional<SafetyGrade> findByDistrictAndCity(String district, String city);
}

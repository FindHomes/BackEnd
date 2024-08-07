package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.SafetyGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyGradeRepository extends JpaRepository<SafetyGrade, Integer> {
}

package com.findhomes.findhomesbe.domain.condition.service;

import com.findhomes.findhomesbe.domain.condition.domain.SafetyGrade;
import com.findhomes.findhomesbe.domain.condition.SafetyGradeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SafetyGradeService {
    private final SafetyGradeRepository safetyGradeRepository;
    public static List<SafetyGrade> safetyGrades;

    @PostConstruct
    public void postConstruct() {
        safetyGrades = safetyGradeRepository.findAll();
    }


    public SafetyGrade getSafetyGradeByAddress(String district, String city) {
        // 새 로직
        return safetyGrades.stream()
                .filter(safetyGrade -> safetyGrade.getDistrict().equals(district) && safetyGrade.getCity().equals(city))
                .findFirst()
                .orElse(null);

        // 기존 로직
//        return safetyGradeRepository.findByDistrictAndCity(district, city).orElse(null);
    }
}

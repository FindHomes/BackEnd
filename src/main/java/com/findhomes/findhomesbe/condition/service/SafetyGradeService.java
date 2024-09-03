package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.entity.SafetyGrade;
import com.findhomes.findhomesbe.repository.SafetyGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SafetyGradeService {
    private final SafetyGradeRepository safetyGradeRepository;

    public SafetyGrade getSafetyGradeByAddress(String district, String city) {
        return safetyGradeRepository.findByDistrictAndCity(district, city).orElse(null);
    }
}

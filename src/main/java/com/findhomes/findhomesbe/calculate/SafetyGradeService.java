package com.findhomes.findhomesbe.calculate;

import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.calculate.data.SafetyEnum;
import com.findhomes.findhomesbe.repository.SafetyGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SafetyGradeService {
    private final SafetyGradeRepository safetyGradeRepository;

    public void insertSafetyGradeInfoInHouseCondition(List<HouseWithCondition> houseWithConditions, Set<SafetyEnum> safetyConditions) {
        houseWithConditions.forEach(houseWithCondition ->
                safetyConditions.forEach(safetyCondition ->
                        // TODO: db에서 실제로 가져오는거 해야됨
                        houseWithCondition.getSafetyGradeMap().put(safetyCondition, 2d)));
    }
}

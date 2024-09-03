package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.PublicData;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.SafetyGrade;
import com.findhomes.findhomesbe.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class HouseWithConditionService {
    private final HouseService houseService;

    public List<HouseWithCondition> convertHouseList(List<House> houses) {
        return houses.stream()
                .map(this::convertHouse)
                .filter(Objects::nonNull)
                .toList();
    }
    private HouseWithCondition convertHouse(House house) {
        String[] districtAndCity = houseService.extractDistrictAndCity(house.getAddress());

        if (districtAndCity == null) {
            return null;
        }

        return new HouseWithCondition(house, districtAndCity[0], districtAndCity[1]);
    }
}

package com.findhomes.findhomesbe.domain.house.mybatis;

import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.house.domain.House;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyBatisHouseRepository {

    private final HouseMapper houseMapper;

    public List<House> findHouse(AllConditions allConditions, int areaLevel, String status) {

        boolean isAirConOption = allConditions.getHouseOptionDataList().stream()
                .anyMatch(option -> "벽걸이형".equals(option.getOption().getHouseOption())
                        || "스탠드형".equals(option.getOption().getHouseOption())
                        || "천장형".equals(option.getOption().getHouseOption()));

        boolean isNonAirConOption = allConditions.getHouseOptionDataList().stream()
                .anyMatch(option -> !"벽걸이형".equals(option.getOption().getHouseOption())
                        && !"스탠드형".equals(option.getOption().getHouseOption())
                        && !"천장형".equals(option.getOption().getHouseOption()));

        return houseMapper.findHouse(allConditions, areaLevel, status, isAirConOption, isNonAirConOption);
    }
}

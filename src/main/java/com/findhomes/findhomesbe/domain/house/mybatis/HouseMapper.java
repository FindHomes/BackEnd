package com.findhomes.findhomesbe.domain.house.mybatis;

import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.house.domain.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper {
    List<House> findHouse(
            @Param("allConditions") AllConditions allConditions,
            @Param("areaLevel") int areaLevel,
            @Param("status") String status,
            @Param("isAirConOption") boolean isAirConOption,
            @Param("isNonAirConOption") boolean isNonAirConOption
    );
}

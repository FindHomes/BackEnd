package com.findhomes.findhomesbe.repository.mybatis;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper {
    List<House> findHouse(
            @Param("allConditions") AllConditions allConditions,
            @Param("status") String status,
            @Param("isAirConOption") boolean isAirConOption,
            @Param("isNonAirConOption") boolean isNonAirConOption
    );
}

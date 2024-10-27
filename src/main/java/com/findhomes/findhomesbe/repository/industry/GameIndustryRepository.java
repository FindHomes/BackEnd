package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.GameIndustry;
import com.findhomes.findhomesbe.entity.industry.GymIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameIndustryRepository extends JpaRepository<GameIndustry,Integer>, IndustryRepository<GameIndustry> {
    @Query("SELECT i FROM GameIndustry i WHERE i.placeName LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<GameIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM GameIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<GameIndustry> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM GameIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<GameIndustry> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);

}

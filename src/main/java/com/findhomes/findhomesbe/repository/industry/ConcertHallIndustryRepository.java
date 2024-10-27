package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.ConcertHallIndustry;
import com.findhomes.findhomesbe.entity.industry.GameIndustry;
import com.findhomes.findhomesbe.entity.industry.GymIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConcertHallIndustryRepository extends JpaRepository<ConcertHallIndustry,Integer>, IndustryRepository<ConcertHallIndustry>{
    @Query("SELECT i FROM ConcertHallIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<ConcertHallIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM ConcertHallIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<ConcertHallIndustry> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM ConcertHallIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<ConcertHallIndustry> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

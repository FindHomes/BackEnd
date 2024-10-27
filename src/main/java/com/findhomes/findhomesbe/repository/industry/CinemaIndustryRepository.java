package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.CinemaIndustry;
import com.findhomes.findhomesbe.entity.industry.ClinicIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CinemaIndustryRepository extends JpaRepository<CinemaIndustry,Integer>, IndustryRepository<CinemaIndustry>{
    @Query("SELECT i FROM CinemaIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<CinemaIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM CinemaIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<CinemaIndustry> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM CinemaIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<CinemaIndustry> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.ClinicIndustry;
import com.findhomes.findhomesbe.entity.industry.ConcertHallIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClinicIndustryRepository extends JpaRepository<ClinicIndustry, Integer>,IndustryRepository<ClinicIndustry> {
    @Query("SELECT i FROM ClinicIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<ClinicIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM ClinicIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<ClinicIndustry> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM ClinicIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<ClinicIndustry> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

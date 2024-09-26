package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.AnimalHospitalIndustry;
import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.BathhouseIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BakeryIndustryRepository extends JpaRepository<BakeryIndustry,Integer>,IndustryRepository<BakeryIndustry> {
    @Query("SELECT i FROM BakeryIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<BakeryIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM BakeryIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<BakeryIndustry> findIndustryWithinBoundary(@Param("districtName") String district, @Param("cityName") String cityName);
}

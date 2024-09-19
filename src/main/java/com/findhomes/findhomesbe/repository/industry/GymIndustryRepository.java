package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.GameIndustry;
import com.findhomes.findhomesbe.entity.industry.GymIndustry;
import com.findhomes.findhomesbe.entity.industry.HospitalIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GymIndustryRepository extends JpaRepository<GymIndustry,Integer>, IndustryRepository<GymIndustry>{
    @Query("SELECT i FROM GymIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<GymIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM GymIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.sigKorNm = :cityName")
    @Override
    List<GymIndustry> findIndustryWithinBoundary(@Param("cityName") String cityName);
}

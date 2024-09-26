package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.AnimalHospitalIndustry;
import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalHospitalIndustryRepository extends JpaRepository<AnimalHospitalIndustry,Integer>,IndustryRepository<AnimalHospitalIndustry> {
    @Query("SELECT i FROM AnimalHospitalIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<AnimalHospitalIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM AnimalHospitalIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<AnimalHospitalIndustry> findIndustryWithinBoundary(@Param("districtName") String district, @Param("cityName") String cityName);
}

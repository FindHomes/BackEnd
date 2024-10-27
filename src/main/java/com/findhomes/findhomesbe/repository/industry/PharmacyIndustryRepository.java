package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.PharmacyIndustry;
import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PharmacyIndustryRepository extends JpaRepository<PharmacyIndustry,Integer>,IndustryRepository<PharmacyIndustry>{
    @Query("SELECT i FROM PharmacyIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<PharmacyIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM PharmacyIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<PharmacyIndustry> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM PharmacyIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<PharmacyIndustry> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

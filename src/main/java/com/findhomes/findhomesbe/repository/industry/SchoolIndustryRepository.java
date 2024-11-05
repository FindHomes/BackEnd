

package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.SchoolIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SchoolIndustryRepository extends JpaRepository<SchoolIndustry, Integer>, IndustryRepository<SchoolIndustry> {
    @Query("SELECT i FROM KaraokeIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<SchoolIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM SchoolIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " + "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<SchoolIndustry> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM SchoolIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " + "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<SchoolIndustry> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

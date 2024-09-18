package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.BeautyIndustry;
import com.findhomes.findhomesbe.entity.industry.PharmacyIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BeautyIndustryRepository extends JpaRepository<BeautyIndustry,Integer>, IndustryRepository {
    @Query("SELECT i FROM BeautyIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<PharmacyIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM BeautyIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.sigKorNm = :cityName")
    @Override
    List<PharmacyIndustry> findIndustryWithinBoundary(@Param("cityName") String cityName);
}

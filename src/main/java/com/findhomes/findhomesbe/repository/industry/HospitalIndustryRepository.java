package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.HospitalIndustry;
import com.findhomes.findhomesbe.entity.industry.KaraokeIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalIndustryRepository extends JpaRepository<HospitalIndustry,Integer>, IndustryRepository<HospitalIndustry>{
    @Query("SELECT i FROM HospitalIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<HospitalIndustry> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM HospitalIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.sigKorNm = :cityName")
    @Override
    List<HospitalIndustry> findIndustryWithinBoundary(@Param("cityName") String cityName);

    @Query("SELECT h FROM HospitalIndustry h WHERE " +
            "h.placeName LIKE %:keyword% OR " +
            "h.roadAddress LIKE %:keyword% OR " +
            "h.category LIKE %:keyword% OR " +
            "h.major LIKE %:keyword%")
    List<HospitalIndustry> findByKeyword(@Param("keyword") String keyword);
}

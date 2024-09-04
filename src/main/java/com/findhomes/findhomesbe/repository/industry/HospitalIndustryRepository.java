package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.HospitalIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalIndustryRepository extends JpaRepository<HospitalIndustry,Integer> {
    @Query("SELECT e FROM HospitalIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<HospitalIndustry> findByDetailName(@Param("detailName") String detailName);
    List<HospitalIndustry> findByPlaceName(String name);

    @Query("SELECT h FROM HospitalIndustry h WHERE " +
            "h.placeName LIKE %:keyword% OR " +
            "h.roadAddress LIKE %:keyword% OR " +
            "h.category LIKE %:keyword% OR " +
            "h.major LIKE %:keyword%")
    List<HospitalIndustry> findByKeyword(@Param("keyword") String keyword);
}

package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.HospitalIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalRepository extends JpaRepository<HospitalIndustry,Integer> {
    List<HospitalIndustry> findByPlaceName(String name);

    @Query("SELECT h FROM HospitalIndustry h WHERE " +
            "h.placeName LIKE %:keyword% OR " +
            "h.roadAddress LIKE %:keyword% OR " +
            "h.category LIKE %:keyword% OR " +
            "h.major LIKE %:keyword%")
    List<HospitalIndustry> findByKeyword(@Param("keyword") String keyword);
}

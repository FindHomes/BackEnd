package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.AnimalHospitalIndustry;
import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BakeryIndustryRepository extends JpaRepository<BakeryIndustry,Integer> {
    @Query("SELECT e FROM BakeryIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<BakeryIndustry> findByDetailName(@Param("detailName") String detailName);
}

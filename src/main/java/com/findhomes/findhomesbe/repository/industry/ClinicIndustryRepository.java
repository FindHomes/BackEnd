package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.ClinicIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClinicIndustryRepository extends JpaRepository<ClinicIndustry, Integer> {
    @Query("SELECT e FROM ClinicIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName% OR e.major LIKE %:detailName%")
    List<ClinicIndustry> findByDetailName(@Param("detailName") String detailName);
}

package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.GameIndustry;
import com.findhomes.findhomesbe.entity.industry.GymIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GymIndustryRepository extends JpaRepository<GymIndustry,Integer> {
    @Query("SELECT e FROM GymIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<GymIndustry> findByDetailName(@Param("detailName") String detailName);
}

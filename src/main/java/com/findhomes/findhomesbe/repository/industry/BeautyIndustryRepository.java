package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.BeautyIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BeautyIndustryRepository extends JpaRepository<BeautyIndustry,Integer> {
    @Query("SELECT e FROM BeautyIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<BeautyIndustry> findByDetailName(@Param("detailName") String detailName);
}

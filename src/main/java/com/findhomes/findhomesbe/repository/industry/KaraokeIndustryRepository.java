package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.KaraokeIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KaraokeIndustryRepository extends JpaRepository<KaraokeIndustry,Integer> {
    @Query("SELECT e FROM KaraokeIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<KaraokeIndustry> findByDetailName(@Param("detailName") String detailName);
}

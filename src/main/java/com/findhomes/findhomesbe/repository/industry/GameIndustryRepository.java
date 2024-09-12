package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.GameIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameIndustryRepository extends JpaRepository<GameIndustry,Integer> {
    @Query("SELECT e FROM GameIndustry e WHERE e.placeName LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<GameIndustry> findByDetailName(@Param("detailName") String detailName);
}

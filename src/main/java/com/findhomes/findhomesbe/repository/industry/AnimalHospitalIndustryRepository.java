package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.AnimalHospitalIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalHospitalIndustryRepository extends JpaRepository<AnimalHospitalIndustry,Integer> {
    @Query("SELECT e FROM AnimalHospitalIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<AnimalHospitalIndustry> findByDetailName(@Param("detailName") String detailName);
}

package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantIndustryRepository extends JpaRepository<RestaurantIndustry, Integer>, JpaSpecificationExecutor<RestaurantIndustry> {
    @Query("SELECT e FROM RestaurantIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<RestaurantIndustry> findByDetailName(@Param("detailName") String detailName);
}

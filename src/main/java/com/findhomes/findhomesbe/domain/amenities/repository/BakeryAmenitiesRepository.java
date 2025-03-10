package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.BakeryAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BakeryAmenitiesRepository extends JpaRepository<BakeryAmenities,Integer>, AmenitiesRepository<BakeryAmenities> {
    @Query("SELECT i FROM BakeryAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<BakeryAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM BakeryAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<BakeryAmenities> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM BakeryAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<BakeryAmenities> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

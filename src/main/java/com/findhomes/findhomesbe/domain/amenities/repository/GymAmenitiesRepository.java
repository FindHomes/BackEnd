package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.GymAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GymAmenitiesRepository extends JpaRepository<GymAmenities,Integer>, AmenitiesRepository<GymAmenities> {
    @Query("SELECT i FROM GymAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<GymAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM GymAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<GymAmenities> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM GymAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<GymAmenities> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

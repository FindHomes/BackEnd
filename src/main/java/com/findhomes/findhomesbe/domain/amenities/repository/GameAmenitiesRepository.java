package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.GameAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameAmenitiesRepository extends JpaRepository<GameAmenities,Integer>, AmenitiesRepository<GameAmenities> {
    @Query("SELECT i FROM GameAmenities i WHERE i.placeName LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<GameAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM GameAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<GameAmenities> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM GameAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<GameAmenities> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);

}

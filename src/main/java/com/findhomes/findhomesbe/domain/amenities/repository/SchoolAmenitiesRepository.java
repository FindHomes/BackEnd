

package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.SchoolAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SchoolAmenitiesRepository extends JpaRepository<SchoolAmenities, Integer>, AmenitiesRepository<SchoolAmenities> {
    @Query("SELECT i FROM KaraokeAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<SchoolAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM SchoolAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " + "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<SchoolAmenities> findAmenitiesInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM SchoolAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " + "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<SchoolAmenities> findAmenitiesInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.ClinicAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClinicAmenitiesRepository extends JpaRepository<ClinicAmenities, Integer>, AmenitiesRepository<ClinicAmenities> {
    @Query("SELECT i FROM ClinicAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<ClinicAmenities> findByDetailName(@Param("detailName") String detailName);

    @Query("SELECT i FROM ClinicAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<ClinicAmenities> findAmenitiesInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM ClinicAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<ClinicAmenities> findAmenitiesInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

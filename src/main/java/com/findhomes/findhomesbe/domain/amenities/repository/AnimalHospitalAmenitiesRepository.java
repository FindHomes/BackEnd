package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.AnimalHospitalAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalHospitalAmenitiesRepository extends JpaRepository<AnimalHospitalAmenities,Integer>, AmenitiesRepository<AnimalHospitalAmenities> {
    @Query("SELECT i FROM AnimalHospitalAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<AnimalHospitalAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM AnimalHospitalAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<AnimalHospitalAmenities> findAmenitiesInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM AnimalHospitalAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<AnimalHospitalAmenities> findAmenitiesInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

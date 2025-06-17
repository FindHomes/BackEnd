package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.HospitalAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalAmenitiesRepository extends JpaRepository<HospitalAmenities,Integer>, AmenitiesRepository<HospitalAmenities> {
    @Query("SELECT i FROM HospitalAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<HospitalAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM HospitalAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<HospitalAmenities> findAmenitiesInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM HospitalAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<HospitalAmenities> findAmenitiesInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);


    @Query("SELECT h FROM HospitalAmenities h WHERE " +
            "h.placeName LIKE %:keyword% OR " +
            "h.roadAddress LIKE %:keyword% OR " +
            "h.category LIKE %:keyword% OR " +
            "h.major LIKE %:keyword%")
    List<HospitalAmenities> findByKeyword(@Param("keyword") String keyword);
}

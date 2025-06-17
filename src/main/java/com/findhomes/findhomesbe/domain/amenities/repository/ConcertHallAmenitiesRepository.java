package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.ConcertHallAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConcertHallAmenitiesRepository extends JpaRepository<ConcertHallAmenities,Integer>, AmenitiesRepository<ConcertHallAmenities> {
    @Query("SELECT i FROM ConcertHallAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<ConcertHallAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM ConcertHallAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<ConcertHallAmenities> findAmenitiesInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM ConcertHallAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<ConcertHallAmenities> findAmenitiesInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

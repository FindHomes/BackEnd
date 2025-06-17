package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.BathhouseAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BathhouseAmenitiesRepository extends JpaRepository<BathhouseAmenities,Integer>, AmenitiesRepository<BathhouseAmenities> {
    @Query("SELECT i FROM BathhouseAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<BathhouseAmenities> findByDetailName(@Param("detailName") String detailName);

    //
    @Query("SELECT i FROM BathhouseAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<BathhouseAmenities> findAmenitiesInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM BathhouseAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<BathhouseAmenities> findAmenitiesInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);
}

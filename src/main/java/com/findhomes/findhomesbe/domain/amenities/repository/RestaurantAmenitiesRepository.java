package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.RestaurantAmenities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantAmenitiesRepository extends JpaRepository<RestaurantAmenities, Integer>, JpaSpecificationExecutor<RestaurantAmenities>, AmenitiesRepository<RestaurantAmenities> {

    @Query("SELECT i FROM RestaurantAmenities i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<RestaurantAmenities> findByDetailName(@Param("detailName") String detailName);

//
    @Query("SELECT i FROM RestaurantAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city LIKE CONCAT('%', :cityName, '%')  ")
    @Override
    List<RestaurantAmenities> findIndustryInSpecialRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query("SELECT i FROM RestaurantAmenities i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<RestaurantAmenities> findIndustryInRegion(@Param("districtName") String district, @Param("cityName") String cityName);

    @Query(value = "SELECT * FROM backup_restaurant_tbl AS c WHERE ST_CONTAINS(ST_Buffer(ST_PointFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326), :distance), c.coordinate)", nativeQuery = true)
    List<RestaurantAmenities> findWithCoordinate(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("distance") double distance);

}

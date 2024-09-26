package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantIndustryRepository extends JpaRepository<RestaurantIndustry, Integer>, JpaSpecificationExecutor<RestaurantIndustry>,IndustryRepository<RestaurantIndustry>{

    @Query("SELECT i FROM RestaurantIndustry i WHERE i.placeName LIKE %:detailName% OR i.category LIKE %:detailName% OR i.placeTags LIKE %:detailName%")
    @Override
    List<RestaurantIndustry> findByDetailName(@Param("detailName") String detailName);

//
    @Query("SELECT i FROM RestaurantIndustry i JOIN Regions rg ON ST_Contains(rg.boundary, i.coordinate) " +
            "WHERE rg.district= :districtName and rg.city = :cityName  ")
    @Override
    List<RestaurantIndustry> findIndustryWithinBoundary(@Param("districtName") String district, @Param("cityName") String cityName);


//    @Query(value = "SELECT * FROM backup_restaurant_tbl AS c WHERE ST_CONTAINS(ST_Buffer(ST_PointFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326), :distance), c.coordinate)", nativeQuery = true)
//    List<RestaurantIndustry> findWithCoordinate(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("distance") double distance);

}

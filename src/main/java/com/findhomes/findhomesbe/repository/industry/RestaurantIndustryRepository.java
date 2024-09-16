package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantIndustryRepository extends JpaRepository<RestaurantIndustry, Integer>, JpaSpecificationExecutor<RestaurantIndustry> {
    @Query("SELECT e FROM RestaurantIndustry e WHERE e.placeName LIKE %:detailName% OR e.category LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<RestaurantIndustry> findByDetailName(@Param("detailName") String detailName);

    @Query(value = "SELECT * FROM backup_restaurant_tbl AS c WHERE ST_CONTAINS(ST_Buffer(ST_PointFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326), :distance), c.coordinate)", nativeQuery = true)
    List<RestaurantIndustry> findWithCoordinate(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("distance") double distance);

    @Query(value = "SELECT b.* FROM backup_restaurant_tbl AS b, regions_tbl as r " +
            "WHERE r.sig_kor_nm = :cityName " +
            "AND ST_Contains(r.boundary, b.coordinate)", nativeQuery = true)
    List<RestaurantIndustry> findRestaurantsWithinBoundary(@Param("cityName") String cityName);


}

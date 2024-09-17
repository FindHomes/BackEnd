package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.locationtech.jts.geom.Geometry;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@ComponentScan
public interface HouseRepository extends JpaRepository<House, Integer>, JpaSpecificationExecutor<House> {
    List<House> findByPriceType(String priceType);

    List<House> findByHousingType(String housingType);
    @Query("SELECT h FROM House h WHERE ST_Contains(:boundary, h.coordinate) = true")
    List<House> findHousesWithinBoundary(@Param("boundary") Geometry boundary);
    @Query(value = "SELECT h.* FROM house_tbl AS h, regions_tbl as r " +
            "WHERE r.sig_kor_nm = :cityName " +
            "AND ST_Contains(r.boundary, h.coordinate)", nativeQuery = true)
    List<House> findHouseWithRegion(@Param("cityName") String cityName);
}

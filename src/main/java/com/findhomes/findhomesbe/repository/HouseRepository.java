package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@ComponentScan
public interface HouseRepository extends JpaRepository<House, Integer>, JpaSpecificationExecutor<House> {
    List<House> findByPriceType(String priceType);

    List<House> findByHousingTypeAndStatus(String housingType, String status);

    @Query(value = "SELECT h.* FROM houses_tbl AS h, regions_tbl as rg " +
            "WHERE rg.city = :cityName "+
            "AND rg.district = :districtName " +
            "AND ST_Contains(rg.boundary, h.coordinate)" +
            "AND h.status=:status", nativeQuery = true)
    List<House> findHouseWithRegion(@Param("districtName") String districtName, @Param("cityName") String cityName, @Param("status") String status);

    @Query(value = "SELECT h.* FROM houses_tbl AS h, regions_tbl as rg " +
            "WHERE rg.city LIKE CONCAT('%', :cityName, '%') " +
            "AND rg.district = :districtName " +
            "AND ST_Contains(rg.boundary, h.coordinate)" +
            "AND h.status=:status", nativeQuery = true)
    List<House> findHouseWithSpecialRegion(@Param("districtName") String districtName, @Param("cityName") String cityName, @Param("status") String status);
}

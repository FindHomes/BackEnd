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
import java.util.stream.Collectors;

@ComponentScan
public interface HouseRepository extends JpaRepository<House, Integer>, JpaSpecificationExecutor<House> {
    List<House> findByPriceType(String priceType);

    List<House> findByHousingTypeAndStatus(String housingType, String status);

    @Query(value = "SELECT h.* FROM houses_tbl AS h, regions_tbl as rg " +
            "WHERE rg.city = :cityName AND rg.district = :districtName " +
            "AND ST_Contains(rg.boundary, h.coordinate)" +
            "AND h.status=:status", nativeQuery = true)
    List<House> findHouseWithRegion(@Param("districtName") String districtName, @Param("cityName") String cityName, @Param("status") String status);


    @Query(value = "SELECT h.* FROM houses_tbl AS h, regions_tbl AS rg " +
            "WHERE rg.city = :cityName AND rg.district = :districtName " +
            "AND ST_Contains(rg.boundary, h.coordinate) " +
            "AND h.status = :status " +
            "AND (:housingTypes IS NULL OR h.housing_type IN (:housingTypes)) " +
            "AND (:priceMm IS NULL OR (h.price_type = '매매' AND h.price <= :priceMm)) " +
            "AND (:priceJs IS NULL OR (h.price_type = '전세' AND h.price <= :priceJs)) " +
            "AND (:priceWsDeposit IS NULL OR (h.price_type = '월세' AND h.price <= :priceWsDeposit AND h.price_for_ws <= :priceWsRent))",
            nativeQuery = true)
    List<House> findHouseWithRegion(
            @Param("districtName") String districtName,
            @Param("cityName") String cityName,
            @Param("status") String status,
            @Param("housingTypes") List<String> housingTypes,
            @Param("priceMm") Integer priceMm,
            @Param("priceJs") Integer priceJs,
            @Param("priceWsDeposit") Integer priceWsDeposit,
            @Param("priceWsRent") Integer priceWsRent
    );

}

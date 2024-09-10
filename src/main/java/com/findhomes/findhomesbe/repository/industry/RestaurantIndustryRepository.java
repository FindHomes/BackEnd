package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.BakeryIndustry;
import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantIndustryRepository extends JpaSpecificationExecutor<RestaurantIndustry>,IndustryRepository<RestaurantIndustry> {
    @Query(value = "SELECT * FROM backup_restaurant_tbl r " +
            "WHERE ST_CONTAINS(ST_Buffer(ST_PointFromText(:point, :srid), :radius), r.coordinate)",
            nativeQuery = true)
    List<RestaurantIndustry> findByLocation(@Param("point") String point,
                                            @Param("srid") int srid,
                                            @Param("radius") double radius);

    @Query(value = "SELECT * FROM backup_restaurant_tbl r " +
            "WHERE ST_CONTAINS(ST_Buffer(ST_PointFromText(:point, :srid), :radius), r.coordinate) " +
            "AND (r.place_name LIKE %:detailName% OR r.category LIKE %:detailName% OR r.place_tags LIKE %:detailName%)",
            nativeQuery = true)
    List<RestaurantIndustry> findByLocationAndDetailName(@Param("point") String point,
                                                         @Param("srid") int srid,
                                                         @Param("radius") double radius,
                                                         @Param("detailName") String detailName);


}

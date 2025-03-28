package com.findhomes.findhomesbe.domain.amenities.repository;

import com.findhomes.findhomesbe.domain.amenities.domain.Amenities;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenitiesRepository<T extends Amenities> {

    List<T> findAmenitiesInRegion(String districtName, String cityName);
    List<T> findAmenitiesInSpecialRegion(String districtName, String cityName);

    List<T> findByDetailName(@Param("detailName") String detailName);

}


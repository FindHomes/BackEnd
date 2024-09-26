package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.Industry;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IndustryRepository<T extends Industry> {

    List<T> findIndustryWithinBoundary(String districtName, String cityName);
    List<T> findByDetailName(@Param("detailName") String detailName);

}


package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.entity.industry.RestaurantIndustry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.findhomes.findhomesbe.entity.Industry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IndustryRepository<T extends Industry> {
    List<T> findIndustryWithinBoundary(@Param("cityName") String cityName);
}


package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.RestaurantIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RestaurantIndustryRepository extends JpaRepository<RestaurantIndustry, Integer>, JpaSpecificationExecutor<RestaurantIndustry> {
}

package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantIndustryRepository extends JpaRepository<Restaurant, Integer> {
}

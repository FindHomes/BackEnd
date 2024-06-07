package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@ComponentScan
public interface HouseRepository extends JpaRepository<House, Integer> {
    List<House> findByPriceType(String priceType);
}

package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.RecentlyViewedHouse;
import com.findhomes.findhomesbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecentlyViewedHouseRepository extends JpaRepository<RecentlyViewedHouse, Integer> {
    Optional<RecentlyViewedHouse> findByUserAndHouse(User user, House house);
}

package com.findhomes.findhomesbe.domain.house.repository;

import com.findhomes.findhomesbe.domain.house.domain.House;
import com.findhomes.findhomesbe.domain.house.domain.RecentlyViewedHouse;
import com.findhomes.findhomesbe.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecentlyViewedHouseRepository extends JpaRepository<RecentlyViewedHouse, Integer> {
    Optional<RecentlyViewedHouse> findByUserAndHouse(User user, House house);
}

package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.FavoriteHouse;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteHouseRepository extends JpaRepository<FavoriteHouse,Integer> {
    Optional<FavoriteHouse> findByUserAndHouse(User user, House house);

}

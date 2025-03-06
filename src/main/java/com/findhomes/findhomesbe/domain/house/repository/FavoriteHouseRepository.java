package com.findhomes.findhomesbe.domain.house.repository;

import com.findhomes.findhomesbe.domain.house.domain.FavoriteHouse;
import com.findhomes.findhomesbe.domain.house.domain.House;
import com.findhomes.findhomesbe.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface FavoriteHouseRepository extends JpaRepository<FavoriteHouse,Integer> {
    Optional<FavoriteHouse> findByUserAndHouse(User user, House house);

    // userId 에 맞는 houseId 가져오기
    @Query("SELECT f.house.houseId FROM FavoriteHouse f WHERE f.user.userId = :userId")
    Set<Integer> findFavoriteHouseIdsByUserId(@Param("userId") String userId);

}

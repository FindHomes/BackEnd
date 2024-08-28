package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {


    Optional<User> findBykakaoId(String kakaoId);
}

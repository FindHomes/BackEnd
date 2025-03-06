package com.findhomes.findhomesbe.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {

    Optional<User> findByRefreshToken(String username);
    Optional<User> findByKakaoId(String kakaoId);
}

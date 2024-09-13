package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionsRepository extends JpaRepository<Regions, Integer> {
    Regions findBysigKorNm(String sigKorNm);
}

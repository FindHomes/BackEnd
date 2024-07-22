package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital,Integer> {
    List<Hospital> findByPlaceName(String name);

    @Query("SELECT h FROM Hospital h WHERE " +
            "h.placeName LIKE %:keyword% OR " +
            "h.roadAddress LIKE %:keyword% OR " +
            "h.category LIKE %:keyword% OR " +
            "h.major LIKE %:keyword%")
    List<Hospital> findByKeyword(@Param("keyword") String keyword);
}

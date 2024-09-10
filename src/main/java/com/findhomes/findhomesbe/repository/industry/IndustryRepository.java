package com.findhomes.findhomesbe.repository.industry;

import com.findhomes.findhomesbe.entity.industry.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IndustryRepository<T extends Industry> extends JpaRepository<T, Integer> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.placeName LIKE %:detailName% OR e.placeTags LIKE %:detailName%")
    List<T> findByDetailName(@Param("detailName") String detailName);
}

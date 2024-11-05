package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.searchlog.SearchLog;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@ComponentScan
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {
    @Query("SELECT COUNT(s) > 0 " +
            "FROM SearchLog s " +
            "WHERE s.user.userId = :userId " +
            "AND s.searchCondition = :searchCondition " +
            "AND s.status = :status")
    boolean existsByUserAndSearchConditionAndStatus(@Param("userId") String userId, @Param("searchCondition") String searchCondition, @Param("status") String status);
}

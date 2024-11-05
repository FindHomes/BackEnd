package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.searchlog.SearchLog;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@ComponentScan
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {
    List<SearchLog> findByUserAndStatus(User user, String active);
}

package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.searchlog.SearchLog;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@ComponentScan
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {
}

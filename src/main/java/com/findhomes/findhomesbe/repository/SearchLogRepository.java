package com.findhomes.findhomesbe.repository;

import com.findhomes.findhomesbe.searchlog.SearchLog;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;

@ComponentScan
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {
}

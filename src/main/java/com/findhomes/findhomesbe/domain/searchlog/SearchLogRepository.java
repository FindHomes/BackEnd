package com.findhomes.findhomesbe.domain.searchlog;

import com.findhomes.findhomesbe.domain.user.User;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@ComponentScan
public interface SearchLogRepository extends JpaRepository<SearchLog, Integer> {
    List<SearchLog> findByUserAndStatus(User user, String active);
}

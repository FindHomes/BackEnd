package com.findhomes.findhomesbe.searchlog;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.SearchLogRepository;
import com.findhomes.findhomesbe.repository.UserRepository;
import com.findhomes.findhomesbe.service.UserService;
import org.assertj.core.api.Assertions;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@SpringBootTest
class SearchLogServiceTest {

    @Autowired
    SearchLogRepository searchLogRepository;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SearchLogService searchLogService;

    String userId = "abcd-abcd-abcd-abcd";
    String kakaoId = "slslslsl";

    @BeforeEach
    void beforeEach() {
        User user = new User();
        user.setUserId(userId);
        user.setStatus("ACTIVE");
        user.setKakaoId(kakaoId);

        userRepository.save(user);
    }

    @Test
    void 검색_기록_추가() {
        // given
        AllConditions allConditions = AllConditions.getExampleAllConditions();
        // when
        searchLogService.addSearchLog(allConditions, userId);
        // then
        List<SearchLog> all = searchLogRepository.findAll();
        Assertions.assertThat(all.get(0).toAllConditions()).isEqualTo(allConditions);
    }

    @Test
    void 검색_기록_삭제() {
        // given
        AllConditions allConditions = AllConditions.getExampleAllConditions();
        User user = userService.getUser(userId);
        searchLogService.addSearchLog(allConditions, userId);
        // when
        SearchLog searchLog = searchLogRepository.findAll().get(0);
        searchLogService.deleteSearchLog(searchLog.getSearchLogId());
        // then
        Assertions.assertThatThrownBy(() -> searchLogService.getSearchLog(searchLog.getSearchLogId()))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void 검색_기록_한번만() {
        // given
        AllConditions allConditions = AllConditions.getExampleAllConditions();
        // when
        for (int i = 0; i < 10; i++) {
            searchLogService.addSearchLog(allConditions, userId);
        }
        // then
        List<SearchLog> all = searchLogRepository.findAll();
        Assertions.assertThat(all.get(0).toAllConditions()).isEqualTo(allConditions);
        Assertions.assertThat(all.size()).isEqualTo(1);
    }
}
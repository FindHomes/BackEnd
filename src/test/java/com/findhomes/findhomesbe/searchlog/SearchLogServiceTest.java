package com.findhomes.findhomesbe.searchlog;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.SearchLogRepository;
import com.findhomes.findhomesbe.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class SearchLogServiceTest {

    @Autowired
    SearchLogRepository searchLogRepository;
    @Autowired
    UserService userService;
    @Autowired
    SearchLogService searchLogService;

    String userId = "b2f1b489-f5bc-417d-86ed-465fe540e7cd";

    @Test
    @Transactional
    void 검색_기록_추가_삭제() {
        // given
        AllConditions allConditions = AllConditions.getExampleAllConditions();
        User user = userService.getUser(userId);

        // when
        searchLogService.addSearchLog(allConditions, userId);

        // then
        SearchLog searchLog = user.getSearchLogList().get(user.getSearchLogList().size() - 1);
        Assertions.assertThat(searchLog.toAllConditions()).isEqualTo(allConditions);

        // 삭제
        //when
        searchLogService.deleteSearchLog(searchLog.getSearchLogId());

        // then
        Assertions.assertThatThrownBy(() -> searchLogService.getSearchLog(searchLog.getSearchLogId()))
                .isInstanceOf(DataNotFoundException.class);
    }
}
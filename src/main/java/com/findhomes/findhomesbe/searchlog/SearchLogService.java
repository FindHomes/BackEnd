package com.findhomes.findhomesbe.searchlog;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.etc.FindHomesUtils;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.SearchLogRepository;
import com.findhomes.findhomesbe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final UserService userService;

    // 검색 기록 추가
    @Transactional
    public void addSearchLog(AllConditions conditions, String userId) {
        User user = userService.getUser(userId);
        SearchLog searchLog = new SearchLog(conditions, "ACTIVE", LocalDateTime.now(), user);

//        boolean exists = searchLogRepository.existsByUserAndSearchConditionAndStatus(userId, searchLog.getSearchCondition(), "ACTIVE");
        boolean exists = user.getSearchLogList().stream()
                .anyMatch(e -> e.getStatus().equals("ACTIVE") && conditions.equals(e.toAllConditions()));

        if (!exists) {
            searchLogRepository.save(searchLog);
            user.getSearchLogList().add(searchLog);
            log.info("[{}] 검색 기록 저장 완료. userId: {}", userId, userId);
        } else {
            log.info("[{}] 이미 같은 검색 기록이 있음.", userId);
        }
    }

    // 검색 기록 삭제
    @Transactional
    public void deleteSearchLog(int searchLogId) {
        searchLogRepository.deleteById(searchLogId);
        log.info("검색 기록 삭제 완료. searchLogId: {}", searchLogId);
    }

    // 검색 기록 목록 가져오기
    public List<SearchLogDto> getSearchLogs(String userId) {
        User user = userService.getUser(userId);
        return user.getSearchLogList().stream()
                .map(this::convertLogToDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 검색 기록 하나 가져오기
    public SearchLog getSearchLog(int searchLogId) {
        Optional<SearchLog> searchLogOptional = searchLogRepository.findById(searchLogId);
        if (searchLogOptional.isEmpty()) {
            throw new DataNotFoundException(searchLogId + " id를 가진 검색 기록이 없습니다.");
        }
        return searchLogOptional.get();
    }

    private SearchLogDto convertLogToDto(SearchLog searchLog) {
        AllConditions allConditions = searchLog.toAllConditions();
        if (allConditions == null)
            return null;

        return new SearchLogDto(
                searchLog.getSearchLogId(),
                FindHomesUtils.calculateTimeAgo(searchLog.getCreatedAt()),
                getKeywordsOfAllConditions(allConditions),
                allConditions.getManConRequest().getRegion().toString(),
                allConditions.getManConRequest().typeInfoToString(),
                allConditions.getManConRequest().getPrices().toString()
        );
    }

    private String getKeywordsOfAllConditions(AllConditions allConditions) {
        return String.join(", ", allConditions.getKeywords());
    }
}

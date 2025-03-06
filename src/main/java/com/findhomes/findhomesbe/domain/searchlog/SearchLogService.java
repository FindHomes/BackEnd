package com.findhomes.findhomesbe.domain.searchlog;

import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.condition.etc.FindHomesUtils;
import com.findhomes.findhomesbe.domain.user.User;
import com.findhomes.findhomesbe.global.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

        try {
            searchLogRepository.save(searchLog);
            log.info("[{}] 검색 기록 저장 완료. userId: {}", userId, userId);
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약 조건 위반으로 인한 예외 처리
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

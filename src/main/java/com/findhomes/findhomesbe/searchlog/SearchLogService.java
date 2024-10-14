package com.findhomes.findhomesbe.searchlog;

import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.etc.FindHomesUtils;
import com.findhomes.findhomesbe.entity.User;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.SearchLogRepository;
import com.findhomes.findhomesbe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
        searchLogRepository.save(searchLog);
        log.info("검색 기록 저장 완료. userId: {}", userId);
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
        return new SearchLogDto(
                searchLog.getSearchLogId(),
                FindHomesUtils.calculateTimeAgo(searchLog.getCreatedAt()),
                summarizeAllConditions(searchLog)
        );
    }

    private String summarizeAllConditions(SearchLog searchLog) {
        AllConditions allConditions = searchLog.toAllConditions();
        return allConditions.summarize();
    }
}

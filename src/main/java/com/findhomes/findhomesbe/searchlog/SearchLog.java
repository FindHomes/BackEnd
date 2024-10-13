package com.findhomes.findhomesbe.searchlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "search_logs_tbl")
public class SearchLog {
    @Id
    private Integer searchLogId;
    @Column(columnDefinition = "json")
    private String searchCondition;
    private String status;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public SearchLog(AllConditions conditions, String status, LocalDateTime createdAt, User user) {
        this.searchCondition = getSearchConditionFromAllConditions(conditions);
        this.status = status;
        this.createdAt = createdAt;
        this.user = user;
    }

    // AllConditions 객체를 JSON 문자열로 변환
    public String getSearchConditionFromAllConditions(AllConditions conditions) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(conditions);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // 예외 처리 필요
            return null; // 변환 실패 시 null로 설정
        }
    }

    // JSON 문자열을 AllConditions 객체로 변환하는 메서드
    public AllConditions toAllConditions() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(this.searchCondition, AllConditions.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // 예외 처리 필요
            return null; // 변환 실패 시 null 반환
        }
    }
}

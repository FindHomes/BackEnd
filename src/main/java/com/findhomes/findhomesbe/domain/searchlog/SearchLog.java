package com.findhomes.findhomesbe.domain.searchlog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.findhomes.findhomesbe.domain.condition.domain.AllConditions;
import com.findhomes.findhomesbe.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "search_logs_tbl")
public class SearchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer searchLogId;
    @Column(columnDefinition = "json")
    private String searchCondition;
    private String searchConditionHash;
    private String status;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public SearchLog(AllConditions conditions, String status, LocalDateTime createdAt, User user) {
        this.searchCondition = getSearchConditionFromAllConditions(conditions);
        this.searchConditionHash = generateHash(this.searchCondition);
        this.status = status;
        this.createdAt = createdAt;
        this.user = user;
    }

    // AllConditions 객체를 JSON 문자열로 변환
    public String getSearchConditionFromAllConditions(AllConditions conditions) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈 등록
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true); // 필드 순서 고정
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // null 값 제외
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 날짜 포맷 일관성 유지

        try {
            return objectMapper.writeValueAsString(conditions);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // 예외 처리 필요
            return null; // 변환 실패 시 null로 설정
        }
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // 해시 생성 실패 시 null 반환
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

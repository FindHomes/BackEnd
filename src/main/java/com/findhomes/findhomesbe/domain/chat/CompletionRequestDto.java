package com.findhomes.findhomesbe.domain.chat;

import lombok.*;

import java.util.List;

/**
 * 프롬프트 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompletionRequestDto {

    private String model;
    private List<Message> messages;
    private double temperature;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}

package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Service
public class ChatService {

    private final ChatGPTService chatGPTService;

    public ChatService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    public String getResponse(String conversation) {
        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a helpful assistant specialized in understanding user preferences for housing searches.")
                        .build(),
                CompletionRequestDto.Message.builder()
                        .role("user")
                        .content(conversation)
                        .build()
        );
        CompletionRequestDto completionRequestDto = CompletionRequestDto.builder()
                .messages(messages)
                .build();

        Map<String, Object> result = chatGPTService.prompt(completionRequestDto, 0.1);

        return parseGPTResponse(result);
    }

    private String parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        return (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");
    }
}
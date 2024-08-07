package com.findhomes.findhomesbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto.Message;
import com.findhomes.findhomesbe.config.ChatGPTConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatGPTServiceImpl implements ChatGPTService {

    private final ChatGPTConfig chatGPTConfig;

    @Value("gpt-4o")
    private String model;

    public ChatGPTServiceImpl(ChatGPTConfig chatGPTConfig) {
        this.chatGPTConfig = chatGPTConfig;
    }

    @Override
    public Map<String, Object> prompt(CompletionRequestDto completionRequestDto) {

        Map<String, Object> result;

        HttpHeaders headers = chatGPTConfig.httpHeaders();

        ObjectMapper om = new ObjectMapper();

        // 모델과 메시지를 추가하여 객체를 구성합니다.
        completionRequestDto = CompletionRequestDto.builder()
                .model(model)
                .messages(completionRequestDto.getMessages()) // 여러 메시지 추가
                .temperature(0)
                .build();

        String requestBody;
        try {
            requestBody = om.writeValueAsString(completionRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = chatGPTConfig.restTemplate()
                .exchange(
                        "https://api.openai.com/v1/chat/completions",
                        HttpMethod.POST,
                        requestEntity,
                        String.class);

        try {
            result = om.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

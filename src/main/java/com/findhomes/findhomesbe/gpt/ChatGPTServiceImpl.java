package com.findhomes.findhomesbe.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.config.ChatGPTConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.findhomes.findhomesbe.gpt.ChatGPTConst.*;
import static com.findhomes.findhomesbe.gpt.CommandService.createCompleteSectionCommand;

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
    public Map<String, Object> prompt(CompletionRequestDto completionRequestDto, Double temperature) {
        Map<String, Object> result;

        HttpHeaders headers = chatGPTConfig.httpHeaders();

        ObjectMapper om = new ObjectMapper();

        // 모델과 메시지를 추가하여 객체를 구성합니다.
        completionRequestDto = CompletionRequestDto.builder()
                .model(model)
                .messages(completionRequestDto.getMessages()) // 여러 메시지 추가
                .temperature(temperature)
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

    public String getGptOutput(
            String command,
            String role1,
            String role2,
            String content,
            double temperature
    ) {
        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role(role1)
                        .content(content)
                        .build(),
                CompletionRequestDto.Message.builder()
                        .role(role2)
                        .content(command)
                        .build()
        );

        CompletionRequestDto completionRequestDto = CompletionRequestDto.builder()
                .messages(messages)
                .build();

        Map<String, Object> result = prompt(completionRequestDto, temperature);

        return parseGPTResponse(result);
    }
    private String parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");
        return content;
    }


    // 비동기로 GPT한테 질문을 섹션 별로 총 5번함 ㅋㅋㅋㅋㅋㅋㅋ
    public String getGptOutputComplete(String conversation) {
        // CompletableFuture로 비동기 병렬처리
        CompletableFuture<String> houseConditionFuture = runAsync(conversation, HouseCondition.getAllData(), HOUSE_CONDITION_DETAIL_REQUEST_COMMAND);
        CompletableFuture<String> houseOptionFuture = runAsync(conversation, HouseOption.getAllData(), HOUSE_OPTION_DETAIL_REQUEST_COMMAND);
        CompletableFuture<String> facilityCategoryFuture = runAsync(conversation, FacilityCategory.getAllData(), FACILITY_CATEGORY_DETAIL_REQUEST_COMMAND);
        CompletableFuture<String> publicDataFuture = runAsync(conversation, PublicData.getAllData(), PUBLIC_DATA_DETAIL_REQUEST_COMMAND);
        CompletableFuture<String> userLocationFuture = runAsync(conversation, null, USER_LOCATION_DETAIL_REQUEST_COMMAND);

        // 모든 CompletableFuture가 완료되면 결과를 반환
        try {
            return CompletableFuture.allOf(houseConditionFuture, houseOptionFuture, facilityCategoryFuture, publicDataFuture, userLocationFuture)
                    .thenApply(v -> {
                        String result = getRefinedString(houseConditionFuture) + "\n" +
                                getRefinedString(houseOptionFuture) + "\n" +
                                getRefinedString(facilityCategoryFuture) + "\n" +
                                getRefinedString(publicDataFuture) + "\n" +
                                getRefinedString(userLocationFuture);
                        log.info("[[최종 응답 결과]]\n{}", result);
                        return result;
                    }).get();
        } catch (Exception e) {
            return "\n\n\n\n\n\n";
        }
    }

    private String getRefinedString(CompletableFuture<String> houseConditionFuture) {
        try {
            return houseConditionFuture.get().trim().replaceAll("[^가-힣0-9\\s\\-()_+,.]", "");
        } catch (Exception ignored) {
            return "";
        }
    }

    public CompletableFuture<String> runAsync(String userInput, String possibleData, String detailRequest) {
        return CompletableFuture.supplyAsync(() ->
            getGptOutput(createCompleteSectionCommand(userInput, possibleData, detailRequest), ROLE1, ROLE2, COMPLETE_CONTENT, COMPLETE_TEMPERATURE)
        );
    }
}

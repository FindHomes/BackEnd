package com.findhomes.findhomesbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto;
import com.findhomes.findhomesbe.DTO.CompletionRequestDto.Message;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.config.ChatGPTConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
                .temperature(0.1)
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
    public String getGptOutput(String command) {
        List<CompletionRequestDto.Message> messages = Arrays.asList(
                CompletionRequestDto.Message.builder()
                        .role("system")
                        .content("You are a machine that returns responses according to a predetermined format. Return the result in the specified format without any extra text.")
                        .build(),
                CompletionRequestDto.Message.builder()
                        .role("user")
                        .content(command)
                        .build()
        );

        CompletionRequestDto completionRequestDto = CompletionRequestDto.builder()
                .messages(messages)
                .build();

        Map<String, Object> result = prompt(completionRequestDto);

        return parseGPTResponse(result);
    }
    private String parseGPTResponse(Map<String, Object> result) {
        // GPT 응답에서 content 부분 추출
        String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) result.get("choices")).get(0).get("message")).get("content");
        return content;
    }

    public String createGPTCommand(String userInput) {
        return String.format(
                "사용자의 조건을 입력받고 그 조건과 관련된 데이터를 찾아 데이터를 활용하여 조건에 맞는 부동산을 추천해주는 앱을 만들고 있습니다." +
                        "사용자 조건 입력: '%s'. 매물 옵션: '%s'. 사용 가능한 시설 데이터: '%s'. 사용 가능한 공공 데이터: '%s'. " +
                        "사용자의 요구 사항을 분석하여 아래 형식에 따라 응답해주세요. 응답은 5가지 섹션으로 구성됩니다. " +
                        "각 섹션은 한 줄에 하나씩 표시됩니다. 전체 응답은 총 5줄로 이루어져있어야 합니다. 개행 문자는 섹션들 사이를 구분하는 기준으로만 사용되며, 각 섹션 사이에는 반드시 하나의 개행 문자만 있어야 합니다." +
                        "또한 어떤 섹션에 내용이 없다고 하더라도 그 줄이 생략되어서는 안되고, 빈 줄이더라도 있어야 합니다. 각 섹션은 그 섹션에 해당하는 줄에 위치하고, 전체 응답은 항상 총 5줄입니다.(즉, 개행 문자는 4개만 있어야 합니다.)" +
                        "\n\n응답 형식에는 5가지 섹션이 있으며 각 섹션은 다음과 같습니다:\n" +
                        "섹션1(반드시 응답의 첫째줄에 위치 해야 함). **매물 조건**: 매물 자체와 관련된 추가 조건을 명시합니다. 이 조건들은 매물의 특성에 대한 사용자 요구를 나타냅니다. " +
                        "매물 조건에는 '%s'이 포함될 수 있습니다. " +
                        "예시: '관리비-10, 복층-false, 분리형-true, 층수-3, 크기-30, 방_수-3, 화장실_수-2, 방향-동, 완공일-20241023' 와 같은 양식으로 나타냅니다. 복층과 분리형은 true나 false로 표현하고 방향은 '%s'으로, 완공일은 날짜로, 크기는 제곱미터 단위로 표현합니다.(평수일 경우 제곱미터로 변환)\n\n" +
                        "섹션2(반드시 응답의 둘째줄에 위치 해야 함). 매물 옵션 : 매물에 관한 옵션 중 사용자에게 필요한 옵션을 명시합니다. 예시: 화재경보기,신발장,옷장.\n\n" +
                        "섹션3(반드시 응답의 셋째줄에 위치 해야 함). **시설 데이터**: 사용자 요구와 관련된 필요한 시설 데이터를 명시하고, 각 시설의 중요도를 가중치로 표시합니다. " +
                        "이 가중치는 해당 시설이 사용자 요구와 얼마나 관련이 있는지를 나타냅니다. 가중치는 1부터 10 사이의 값입니다." +
                        "예를 들어 '음식점_버거킹-0.3'는 사용자가 버거킹과 가까운 집을 원할 경우, 음식점 중에서 버거킹의 중요도가 0.3임을 의미합니다. " +
                        "'음식점_버거킹'과 같은 형식에서 '음식점'은 큰 범주를, '버거킹'은 그 범주 안의 특정 키워드를 의미합니다. " +
                        "이처럼 포함관계에 속하는 데이터는 '_'로 표시합니다. 특정 카테고리 전부일 경우 특정 이름이 아니라 all로 표시합니다." +
                        "예시: '음식점_버거킹-3, 피시방_all-2, 미용실_all-1, 병원_이비인후과-4, 병원_소아과-3'.\n\n" +
                        "섹션4(반드시 응답의 넷째줄에 위치 해야 함). **공공 데이터**: 공공 데이터와 관련된 항목을 명시하고, 각 항목의 중요도를 가중치로 표시합니다. " +
                        "이 가중치는 해당 공공 데이터가 사용자 요구와 얼마나 관련이 있는지를 나타냅니다. 예를 들어 '범죄율-3'는 " +
                        "사용자가 안전한 지역을 원할 경우, 범죄율이 7의 중요도를 가지는 것을 의미합니다. " +
                        "예시: '교통사고율-2, 화재율-1, 범죄율-7, 생활안전-2, 자살율-3'.\n\n" +
                        "섹션5(반드시 응답의 다섯째줄에 위치 해야 함). **특정 좌표**: 사용자가 가까웠으면 하는 특정 지점의 좌표입니다. 위도와 경도는 '+'로 구분되고 (37+127)과 같이 표현됩니다. 다음과 같은 양식을 지켜야합니다." +
                        "예를 들어 '네이버 본사와 강남역이랑 가까웠으면 좋겠다'와 같은 조건을 받으면 네이버본사_(37.359512+127.105220)-2, 강남역_(37.497940+127.027620)-2 와 같이 나타냅니다." +
                        "\n모든 섹션의 형식을 정확히 준수하여, 불필요한 텍스트 없이 응답해주세요. 반환 형식 예시는 다음과 같습니다." +
                        "'관리비-20, 층수-3, 복층-true\n가스레인지,샤워부스\n음식점_버거킹-5, 피시방_all-2, 미용실_all-1, 병원_이비인후과-3\n교통사고율-3, 화재율-1, 범죄율-4\n강남역_(37.497940+127.027620)-3'." +
                        "응답은 항상 한글이어야 하고 반드시 예시처럼 미사어구 없이 반환해야합니다.. '\\n'는 섹션들 사이에서 구분하는 기준으로만 쓰여야 하고, 이 문자는 항상 반드시 총 4개여야 합니다. 만약에 없는 섹션이 없다면 개행으로 비워둬야합니다.",
                userInput, HouseOption.getAllData(), FacilityCategory.getAllData(), PublicData.getAllData(), HouseCondition.getAllData(), HouseDirection.getAllData()
        );
    }
// 신버전인데 응답이 이상해서 일단 제외
//    private String createGPTCommand(String userInput) {
//        return String.format(
//                "부동산 추천 앱을 위해 사용자의 조건을 입력받고, 그 조건에 맞는 매물과 관련된 데이터를 찾아 최적의 부동산을 추천하고 있습니다. " +
//                        "사용자는 다음과 같은 조건을 입력했습니다: '%s'. 이 조건을 기반으로 하여, 아래의 각 항목에 맞춰 응답을 구성해주세요. " +
//                        "응답은 반드시 사용자가 입력한 조건과 **직접적으로 관련된 정보만 포함**해야 합니다. " +
//                        "사용자가 언급하지 않은 옵션이나 조건을 절대로 응답에 포함해서는 안 됩니다. " +
//                        "응답은 정확히 5줄로 구성되어야 하며, 각 줄은 하나의 항목을 나타냅니다. 각 항목은 해당 줄에 정확히 위치해야 하며, 항목들 사이에는 정확히 하나의 실제 줄바꿈(개행)이 있어야 합니다. " +
//                        "응답의 형식은 정확히 지켜져야 하며, 불필요한 공백이나 추가 문자가 포함되지 않아야 합니다." +
//                        "\n응답 형식은 다음과 같은 5개의 항목으로 구성됩니다:\n" +
//
//                        "1. **매물 조건** (반드시 첫 번째 줄에 위치): 매물의 특성에 대한 사용자의 추가 요구사항을 명시합니다. " +
//                        "반드시 사용자가 입력한 조건과 **직접적으로 관련된 정보만 포함**해야 하며, 사용자가 언급하지 않은 조건(예: 방 개수, 화장실 개수, 방향 등)은 절대로 응답에 포함되지 않아야 합니다. " +
//                        "관련 조건의 예시로는 다음과 같은 요소가 포함될 수 있습니다: '%s'. " +
//                        "방향 관련 조건이 있다면 반환 양식은 다음 중 하나로 명시되어야 합니다: '%s'. " +
//                        "예시: '관리비-20, 복층-false, 분리형-true, 층수-2, 크기-30, 방_수-1, 화장실_수-1, 방향-남, 완공일-20241023'. " +
//                        "사용자가 입력한 조건과 관련된 정보가 전혀 없다면 이 항목은 빈 줄로 남겨두세요.\n" +
//
//                        "2. **매물 옵션** (반드시 두 번째 줄에 위치): 사용자가 원하는 매물에 포함되어야 하는 옵션을 나열합니다. " +
//                        "반드시 사용자가 입력한 조건과 **직접적으로 관련이 있는 옵션만 포함**해야 합니다. 사용자가 언급하지 않은 옵션(예: 냉장고, 세탁기, 인덕션 등)은 절대로 응답에 포함되지 않아야 합니다. " +
//                        "사용 가능한 매물 옵션 목록: '%s'. " +
//                        "예시: '화재경보기,신발장,옷장'. 사용자가 입력한 조건과 매물 옵션이 관련이 없다면 이 항목은 빈 줄로 남겨두세요.\n" +
//
//                        "3. **시설 데이터** (반드시 세 번째 줄에 위치): 사용자가 원하는 시설과 해당 시설의 중요도를 가중치로 표시합니다. 가중치는 1에서 10 사이의 값으로, 시설이 사용자 요구에 얼마나 중요한지를 나타냅니다. " +
//                        "사용 가능한 시설 데이터 목록: '%s'. 사용자가 입력한 시설에 대해서만 응답하며, 명시되지 않은 시설은 포함하지 마세요. 예시: '음식점_버거킹-8, 피시방_all-2, 미용실_all-1, 병원_이비인후과-4, 병원_소아과-3'. " +
//                        "사용자가 요청한 시설과 관련된 데이터가 없을 경우 이 항목은 빈 줄로 남겨두세요.\n" +
//
//                        "4. **공공 데이터** (반드시 네 번째 줄에 위치): 사용자 요구와 관련된 공공 데이터를 명시하고, 각 데이터의 중요도를 가중치로 표시합니다. " +
//                        "가중치는 1에서 10 사이의 값으로, 공공 데이터의 중요도를 나타냅니다. 사용 가능한 공공 데이터 목록: '%s'. " +
//                        "사용자가 명시한 조건과 관련된 공공 데이터만 응답하며, 명시되지 않은 항목은 포함하지 마세요. " +
//                        "예시: '범죄율-8, 생활안전-7'. 사용자가 요청한 공공 데이터와 관련된 항목이 없을 경우 이 항목은 빈 줄로 남겨두세요.\n" +
//
//                        "5. **특정 좌표** (반드시 다섯 번째 줄에 위치): 사용자가 선호하는 특정 지점의 좌표를 표시합니다. 예를 들어 '강남역_(37.497940+127.027620)-5'. " +
//                        "사용자가 입력한 특정 지점이 없는 경우, 이 항목은 빈 줄로 남겨두세요.\n" +
//                        "응답은 반드시 한글로 작성되어야 하며, 각 항목은 정의된 형식을 준수해야 합니다. 특정 항목에 대한 정보가 없더라도 줄바꿈을 포함해야 합니다. " +
//                        "응답은 정확히 5개의 줄로 구성되어야 하며, 각 줄은 실제 줄바꿈(개행 문자)만을 포함해야 합니다. 마지막 줄 이후에 불필요한 개행 문자가 없어야 합니다." +
//                        "\n모든 항목의 형식을 정확히 준수하여, 불필요한 텍스트 없이 응답해주세요.",
//                userInput, HouseCondition.getAllData(), HouseDirection.getAllData(), HouseOption.getAllData(), FacilityCategory.getAllData(), PublicData.getAllData()
//        );
//    }
}

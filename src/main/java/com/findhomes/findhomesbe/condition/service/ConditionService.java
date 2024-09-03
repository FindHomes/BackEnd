package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.condition.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {
    private final ParsingService parsingService;

    public SearchResponse exec(ManConRequest manConRequest, String gptOutput) {
        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput);
        log.info("===========조건 파싱 결과===========\n{}", allConditions);

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)


        // 2. 공공 데이터 조건 처리

        // 3. 시설 조건 및 사용자 요청 위치 조건 처리


        return new SearchResponse(true, 200, "성공", null);
    }

}

package com.findhomes.findhomesbe.condition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {

    public List<Map<String, String>> parsingGptOutput(String gptOutput) {
        log.info("gpt응답:{}", gptOutput);
        return null;
    }
}

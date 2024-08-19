package com.findhomes.findhomesbe.condition;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.calculate.CalculateService;
import com.findhomes.findhomesbe.calculate.CoordService;
import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.calculate.data.SafetyEnum;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {

    public SearchResponse.SearchResult exec(ManConRequest manConRequest, String gptOutput) {


//        Map<String, List<Industry>> facilitiesMap = new HashMap<>();
//        for (String facilityName : parsingFacilityMap.keySet()) {
//            facilitiesMap.put(facilityName, restaurantIndustryService.getRestaurantByKeyword(facilityName));
//            log.info("시설 {} 개수: {}개", facilityName, facilitiesMap.get(facilityName).size());
//        }
//        // 3-2. 거리 기준으로 매물 필터링
//        List<HouseWithCondition> resultHouseWithConditions = CoordService.filterAndCalculateByFacility(this.preHouseData, facilitiesMap, RADIUS);
//
//        log.info("시설 필터링 후 매물 개수: {}개", resultHouseWithConditions.size());
//
//        // 3-3. 매물에 공공 데이터 정보 넣기
//        Map<SafetyEnum, Double> parsingSafetyMap = new HashMap<>();
//        parsingResult.get(2).forEach((key, value) -> {
//            try {
//                parsingSafetyMap.put(SafetyEnum.valueOf(key), Double.parseDouble(value));
//            } catch (NumberFormatException e) {
//                log.error("GPT응답 파싱에서 공공데이터 가중치가 잘못됨. {}", e.getMessage());
//            } catch (IllegalArgumentException e) {
//                log.error("GPT응답 파싱에서 공공데이터 이름이 잘못됨. {}", e.getMessage());
//            }
//        });
//        safetyGradeService.insertSafetyGradeInfoInHouseCondition(resultHouseWithConditions, parsingSafetyMap.keySet());
//
//        // 4-1. 점수 계산 및 정렬
//        List<House> resultHouses = CalculateService.calculateScore(resultHouseWithConditions, parsingFacilityMap, parsingSafetyMap);
//        this.preHouseData = resultHouses;
//        // 응답 생성 및 반환
//        SearchResponse.SearchResult searchResult = houseService.makeResponse(resultHouses.subList(0, Math.min(100, resultHouses.size())));


        return null;
    }

    public List<Map<String, String>> parsingGptOutput(String gptOutput, String splitRegex) {
        List<Map<String, String>> results = new ArrayList<>();
        String[] sentences = gptOutput.split(splitRegex);

        for (String sentence : sentences) {
            HashMap<String, String> newMap = new HashMap<>();
            String trimmedSentence = sentence.trim();
            String[] conditions = trimmedSentence.split(",");
            for (String condition : conditions) {
                newMap.put(condition.split("-")[0].trim(), condition.split("-")[1].trim());
            }
            results.add(newMap);
        }

        return results;
    }

    public List<House> houseConditionProcessing() {
        return null;
    }

    public List<HouseWithCondition> facilityConditionProcessing() {
        return null;
    }

    public List<HouseWithCondition> publicDataConditionProcessing() {
        return null;
    }
}

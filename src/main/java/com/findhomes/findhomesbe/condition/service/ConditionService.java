package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {
    private final ParsingService parsingService;

    public SearchResponse exec(ManConRequest manConRequest, String gptOutput) {
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput);

        log.info("===========조건 파싱 결과===========\n{}", allConditions);

        // 1. 필터링 조건으로 매물 필터링 하기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)

        // 2. 공공 데이터 조건 처리

        // 3. 시설 조건 및 사용자 요청 위치 조건 처리


        return new SearchResponse(true, 200, "성공", null);
    }

    private Object[] houseConditionPreprocessing(Map<String, String> houseCondition) {
        // 관리비, 복층, 분리형, 층수, 크기, 방 수, 화장실 수, 방향, 완공일, 옵션
        Object[] dataParsing = new Object[]{0, false, false, 0, 0, 0, 0, "동", LocalDate.now(), ""};
        // 조건 전처리
        for (Map.Entry<String, String> entry : houseCondition.entrySet()) {
            switch (entry.getKey()) {
                case "관리비":
                    dataParsing[0] = entry.getValue();
                    break;
                case "복층":
                    dataParsing[1] = entry.getValue();
                    break;
                case "분리형":
                    dataParsing[2] = entry.getValue();
                    break;
                case "층수":
                    dataParsing[3] = toInteger(entry.getValue());
                    break;
                case "크기":
                    dataParsing[4] = entry.getValue();
                    break;
                case "방 수":
                    dataParsing[5] = toInteger(entry.getValue());
                    break;
                case "화장실 수":
                    dataParsing[6] = toInteger(entry.getValue());
                    break;
                case "방향":
                    dataParsing[7] = entry.getValue();
                    break;
                case "완공일":
                    dataParsing[8] = entry.getValue();
                    break;
                case "옵션":
                    dataParsing[9] = entry.getValue();
                    break;
            }
        }

        return null;
    }

    private static int toInteger(String value) {
        String floorStr = value.replaceAll("[^0-9]", "");
        return floorStr.isEmpty() ? -1 : Integer.parseInt(floorStr);
    }


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
}

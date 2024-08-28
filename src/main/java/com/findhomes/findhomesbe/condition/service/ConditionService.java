package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public SearchResponse exec(ManConRequest manConRequest, String gptOutput) {


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

        System.out.println(HouseCondition.getAllData());
        System.out.println(HouseOption.getAllData());
        System.out.println(FacilityCategory.getAllData());
        System.out.println(PublicData.getAllData());
        System.out.println(HouseDirection.getAllData());


        return new SearchResponse(true, 200, "성공", null);
    }

    // 방 수-2, 화장실 수-2, 층수-1
    // / 옵션 종류
    // /음식점_버거킹-10
    // /교통사고율-0, 화재율-0, 범죄율-0, 생활안전-0, 자살율-0, 감염병율-0
    // /네이버 본사_(37.359512+127.105220)-10
    /**
     * HouseCondition.getAllData() -> 매물 필수 조건
     * HouseOption.getAllData() -> 매물 필수 옵션
     * FacilityCategory.getAllData() -> 매물 시설
     * PublicData.getAllData() -> 공공데이터 및 가중치
     */



    /**
     * 매물 자체 조건 파싱
     * @param houseConditionStr - 형태: 방 수-2, 화장실 수-2, 층수-1
     * @return not null. 빈 Map을 반환할 수 있음.
     */
    public Map<HouseCondition, String> parsingHouseCondition(String houseConditionStr) {
        // 결과
        Map<HouseCondition, String> resultMap = new HashMap<>();
        // 예외 처리
        if (houseConditionStr == null || houseConditionStr.isEmpty()) {
            return resultMap;
        }
        // HouseCondition Enum 클래스의 Map으로 바꿈.
        String[] conditions = houseConditionStr.trim().toLowerCase().split(",");
        for (String condition : conditions) {
            try {
                String[] parts = condition.split("-");
                if (parts.length != 2) {
                    log.error("GPT가 옳지 않은 매물 조건 응답을 반환함. 해당 조건: {}", condition);
                    continue;
                }

                String conditionKey = parts[0].trim();
                String conditionValue = parts[1].trim();

                HouseCondition conditionObj = HouseCondition.valueOf(conditionKey.toUpperCase());
                resultMap.put(conditionObj, conditionValue);
            } catch (IllegalArgumentException | NullPointerException exception) {
                log.error("GPT가 옳지 않은 매물 조건 응답을 반환함. 해당 조건: {}", condition);
            }
        }
        return resultMap;
    }

    /**
     * 매물 옵션 조건 파싱
     * @param houseOptionStr - 형태: 인덕션, 세탁기
     * @return not null. 빈 List를 반환할 수 있음.
     */
    public List<HouseOption> parsingOptionCondition(String houseOptionStr) {
        // 결과
        List<HouseOption> resultList = new ArrayList<>();
        // 예외 처리
        if (houseOptionStr == null || houseOptionStr.isEmpty()) {
            return resultList;
        }
        // HouseOption Enum 클래스의 List로 바꿈.
        String[] conditions = houseOptionStr.trim().toLowerCase().split(",");
        for (String condition : conditions) {
            try {
                HouseOption optionObj = HouseOption.valueOf(condition.trim());
                resultList.add(optionObj);
            } catch (IllegalArgumentException | NullPointerException exception) {
                log.error("GPT가 옳지 않은 매물 조건 응답을 반환함. 해당 조건: {}", condition);
            }
        }
        return resultList;
    }

//    /**
//     * 시설 조건 파싱
//     * @param facilityConditionStr - 형태: 음식점_버거킹-10, 병원_이비인후과-5
//     * @return not null. 빈 Map을 반환할 수 있음.
//     */
//    public Map<FacilityCategory, List<String>> parsingFacilityCondition(String facilityConditionStr) {
//        // 결과
//        Map<FacilityCategory, List<String>> resultMap = new HashMap<>();
//        // 예외 처리
//        if (facilityConditionStr == null || facilityConditionStr.isEmpty()) {
//            return resultMap;
//        }
//        // FacilityCategory Enum 클래스의 Map으로 바꿈.
//        String[] conditions = facilityConditionStr.trim().toLowerCase().split(",");
//        for (String condition : conditions) {
//            String[] parts = condition.split("_");
//            if (parts.length != 2) {
//                log.error("GPT가 옳지 않은 매물 조건 응답을 반환함. 해당 조건: {}", condition);
//                continue;
//            }
//
//
//        }
//    }

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

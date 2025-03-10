package com.findhomes.findhomesbe.domain.condition.service;

import com.findhomes.findhomesbe.domain.condition.dto.ManConRequest;
import com.findhomes.findhomesbe.domain.condition.domain.*;
import com.findhomes.findhomesbe.global.exception.exception.IllegalGptOutputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ParsingService {
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
     * 메인 함수
     * @param manConRequest
     * @param gptOutput
     * @return
     */
    public AllConditions parsingGptOutput(ManConRequest manConRequest, String gptOutput, List<String> keywords) {
        AllConditions result = new AllConditions(manConRequest, keywords);

        String[] gptOutputs = gptOutput.split("\n", -1);

        if (gptOutputs.length < 5) {
            log.error("gpt 응답이 5줄이 아닙니다. {}줄 입니다. gpt 응답: {}", gptOutputs.length, gptOutput);
            throw new IllegalGptOutputException("gpt 응답이 올바르지 않습니다.");
        }

        parsingHouseCondition(result, gptOutputs[0]);
        parsingOptionCondition(result, gptOutputs[1]);
        parsingFacilityCondition(result, gptOutputs[2]);
        parsingPublicDataCondition(result, gptOutputs[3]);
        parsingUserRequestLocationCondition(result, gptOutputs[4]);

        return result;
    }

    private String[] splitKeywordAndCondition(List<String> keywords, String str) {
        String[] split = str.split("@");
        if (split.length != 2) {
            throw new IllegalGptOutputException("gpt가 잘못된 응답을 반환함.");
        }
        split[0] = split[0].trim();
        split[1] = split[1].trim();
        if (!keywords.contains(split[0])) {
            throw new IllegalGptOutputException("gpt가 잘못된 키워드를 선택함.");
        }
        return split;
    }

    private void conditionAlreadyContains(List<String> conditions, String condition) {
        if (conditions.contains(condition)) {
            throw new IllegalGptOutputException("gpt가 이미 선택한 조건을 또 선택함.");
        } else {
            conditions.add(condition);
        }
    }

    /**
     * 매물 자체 조건 파싱
     * @param houseConditionStr - 형태: 방 수-2, 화장실 수-2, 층수-1
     * @return not null. 빈 Map을 반환할 수 있음.
     */
    private void parsingHouseCondition(AllConditions allConditions, String houseConditionStr) {
        // 예외 처리
        if (houseConditionStr == null || houseConditionStr.isEmpty()) {
            return;
        }
        //
        String[] conditions = houseConditionStr.trim().toLowerCase().split(",");
        List<String> addedCons = new ArrayList<>();
        for (String condition : conditions) {
            try {
                String[] keywordAndCondition = splitKeywordAndCondition(allConditions.getKeywords(), condition);

                String[] parts = keywordAndCondition[1].split("-");
                if (parts.length != 2) {
                    log.error("GPT가 옳지 않은 매물 조건 응답을 반환함.");
                    continue;
                }

                String conditionKey = parts[0].trim();
                String conditionValue = parts[1].trim();

                conditionAlreadyContains(addedCons, conditionKey);

                HouseCondition conditionObj;
                try {
                    conditionObj = HouseCondition.valueOf(conditionKey.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalGptOutputException("GPT가 옳지 않은 <매물 조건> 응답을 반환함.");
                } catch (Exception e) {
                    throw new Exception("GPT가 옳지 않은 <매물 조건> 조건 응답을 반환함.");
                }

                Object processedConditionValue = conditionObj.parse(conditionValue);

                allConditions.getHouseConditionDataList().add(new AllConditions.HouseConditionData(keywordAndCondition[0], conditionObj, processedConditionValue));
            } catch (Exception e) {
                log.error(e.getMessage() + " 해당 조건: {}", condition);
            }
        }
    }

    /**
     * 매물 옵션 조건 파싱
     * @param houseOptionStr - 형태: 인덕션, 세탁기
     * @return not null. 빈 List를 반환할 수 있음.
     */
    private void parsingOptionCondition(AllConditions allConditions, String houseOptionStr) {
        // 예외 처리
        if (houseOptionStr == null || houseOptionStr.isEmpty()) {
            return;
        }
        //
        String[] conditions = houseOptionStr.trim().toUpperCase().split(",");
        List<String> addedCons = new ArrayList<>();
        for (String condition : conditions) {
            try {
                String[] keywordAndCondition = splitKeywordAndCondition(allConditions.getKeywords(), condition);

                conditionAlreadyContains(addedCons, keywordAndCondition[1].trim());

                HouseOption optionObj;
                try {
                    optionObj = HouseOption.valueOf(keywordAndCondition[1].trim());
                } catch (IllegalArgumentException e) {
                    throw new IllegalGptOutputException("GPT가 옳지 않은 <매물 옵션> 응답을 반환함.");
                } catch (Exception e) {
                    throw new Exception("GPT가 옳지 않은 <매물 옵션> 조건 응답을 반환함.");
                }

                allConditions.getHouseOptionDataList().add(new AllConditions.HouseOptionData(keywordAndCondition[0], optionObj));
            } catch (Exception e) {
                log.error(e.getMessage() + " 해당 조건: {}", condition);
            }
        }
    }

    /**
     * 시설 조건 파싱
     * @param facilityConditionStr - 형태: 음식점_버거킹-10, 병원_이비인후과-5
     * @return not null. 빈 Map을 반환할 수 있음.
     */
    private void parsingFacilityCondition(AllConditions allConditions, String facilityConditionStr) {
        // 예외 처리
        if (facilityConditionStr == null || facilityConditionStr.isEmpty()) {
            return;
        }
        //
        String[] conditions = facilityConditionStr.trim().toLowerCase().split(",");
        List<String> addedCons = new ArrayList<>();
        for (String condition : conditions) {
            try {
                String[] keywordAndCondition = splitKeywordAndCondition(allConditions.getKeywords(), condition);

                String[] parts = keywordAndCondition[1].split("_");
                if (parts.length != 2) {
                    log.error("GPT가 옳지 않은 <시설 조건> 응답을 반환함.");
                    continue;
                }
                FacilityCategory facilityCategory;
                try {
                    facilityCategory = FacilityCategory.valueOf(parts[0].trim());
                } catch (IllegalArgumentException e) {
                    throw new IllegalGptOutputException("GPT가 옳지 않은 <시설 조건> 응답을 반환함.");
                } catch (Exception e) {
                    throw new Exception("GPT가 옳지 않은 <시설 조건> 조건 응답을 반환함.");
                }
                String[] nameAndWeight = parts[1].split("-");
                if (nameAndWeight.length != 2) {
                    log.error("GPT가 옳지 않은 <시설 조건> 응답을 반환함. 해당 조건: {}", condition);
                    continue;
                }

                conditionAlreadyContains(addedCons, keywordAndCondition[1].split("-")[0]);

                allConditions.getFacilityConditionDataList().add(new AllConditions.FacilityConditionData(
                        keywordAndCondition[0], facilityCategory, nameAndWeight[0].trim(), Integer.parseInt(nameAndWeight[1].trim()), facilityCategory.getMaxRadius()
                ));
            } catch (Exception e) {
                log.error(e.getMessage() + " 해당 조건: {}", condition);
            }
        }
    }

    /**
     * 공공 데이터 조건 파싱
     * @param allConditions - 형태: 교통사고율-0, 화재율-0
     * @param publicDataConditionStr
     */
    private void parsingPublicDataCondition(AllConditions allConditions, String publicDataConditionStr) {
        // 예외 처리
        if (publicDataConditionStr == null || publicDataConditionStr.isEmpty()) {
            return;
        }
        //
        String[] conditions = publicDataConditionStr.trim().toLowerCase().split(",");
        for (String condition : conditions) {
            try {
                String[] keywordAndCondition = splitKeywordAndCondition(allConditions.getKeywords(), condition);

                String[] parts = keywordAndCondition[1].split("-");
                if (parts.length != 2) {
                    log.error("GPT가 옳지 않은 <공공데이터> 조건 응답을 반환함.");
                    continue;
                }

                String conditionKey = parts[0].trim();
                String conditionValue = parts[1].trim();

                PublicData conditionObj;
                try {
                    conditionObj = PublicData.valueOf(conditionKey.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalGptOutputException("GPT가 옳지 않은 <공공데이터> 응답을 반환함.");
                } catch (Exception e) {
                    throw new Exception("GPT가 옳지 않은 <공공데이터> 조건 응답을 반환함.");
                }

                allConditions.getPublicConditionDataList().add(new AllConditions.PublicConditionData(keywordAndCondition[0], conditionObj, Integer.parseInt(conditionValue)));
            } catch (Exception e) {
                log.error(e.getMessage() + " 해당 조건: {}", condition);
            }
        }
    }

    /**
     * 사용자 요청 위치 조건 파싱
     * @param allConditions - 형태: 네이버 본사_(37.359512+127.105220)-10, 카카오_(00.0000+00.0000)-5
     * @param userRequestLocationConditionStr
     */
    private void parsingUserRequestLocationCondition(AllConditions allConditions, String userRequestLocationConditionStr) {
        // 예외 처리
        if (userRequestLocationConditionStr == null || userRequestLocationConditionStr.isEmpty()) {
            return;
        }
        //
        String[] conditions = userRequestLocationConditionStr.trim().toLowerCase().split(",");
        for (String condition : conditions) {
            try {
                // 이름과 위치+가중치를 '_'로 분리
                String[] nameAndLocation = condition.split("_");
                if (nameAndLocation.length != 2) {
                    log.error("GPT가 옳지 않은 <사용자 요청 위치 조건> 응답을 반환함. 해당 조건: {}", condition);
                    continue;
                }

                // 이름 부분
                String locationName = nameAndLocation[0].trim();

                // 위치와 가중치 부분을 '-'로 분리
                String[] locationAndWeight = nameAndLocation[1].split("-");
                if (locationAndWeight.length != 2) {
                    log.error("GPT가 옳지 않은 <사용자 요청 위치 조건> 응답을 반환함. 해당 조건: {}", condition);
                    continue;
                }

                // 위치(위도+경도) 부분을 파싱
                String[] latLong = locationAndWeight[0].replace("(", "").replace(")", "").split("\\+");
                if (latLong.length != 2) {
                    log.error("GPT가 옳지 않은 <사용자 요청 위치 조건> 응답을 반환함. 해당 조건: {}", condition);
                    continue;
                }

                Double latitude = Double.parseDouble(latLong[0].trim());
                Double longitude = Double.parseDouble(latLong[1].trim());

                // 가중치 부분을 파싱
                Integer weight = Integer.parseInt(locationAndWeight[1].trim());

                // UserRequestLocationData 객체 생성 및 리스트에 추가
                allConditions.getUserRequestLocationDataList().add(new AllConditions.UserRequestLocationData(
                        locationName, latitude, longitude, weight
                ));
            } catch (Exception e) {
                log.error("GPT가 옳지 않은 <사용자 요청 위치 조건> 응답을 반환함. 해당 조건: {}", condition);
            }
        }
    }

}

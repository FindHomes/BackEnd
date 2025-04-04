package com.findhomes.findhomesbe.domain.condition.service;

import com.findhomes.findhomesbe.domain.condition.dto.ManConRequest;
import com.findhomes.findhomesbe.domain.condition.domain.*;
import com.findhomes.findhomesbe.domain.condition.domain.SessionKeys;
import com.findhomes.findhomesbe.domain.house.domain.House;
import com.findhomes.findhomesbe.domain.house.repository.FavoriteHouseRepository;
import com.findhomes.findhomesbe.domain.house.service.FavoriteHouseService;
import com.findhomes.findhomesbe.domain.house.service.HouseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {
    private final ParsingService parsingService;
    private final HouseService houseService;
    private final HouseWithConditionService houseWithConditionService;
    private final PublicDataService publicDataService;
    private final IndustryService industryService;
    private final FavoriteHouseService favoriteHouseService;
    private final FavoriteHouseRepository favoriteHouseRepository;

    public List<HouseWithCondition> exec(ManConRequest manConRequest, String gptOutput, List<String> keywords, HttpSession session, String userId) {

        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput, keywords);
        session.setAttribute(SessionKeys.ALL_CONDITIONS, allConditions);
        log.info("\n===========조건 파싱 결과===========\n{}", allConditions);

        return exec2(allConditions, userId);
    }

    // 검색 기록 저장한 곳에서 바로 매물 추천 결과 가져오기 위해서 함수 따로 뺐음.
    public List<HouseWithCondition> exec2(AllConditions allConditions, String userId) {
        ManConRequest.Region region = allConditions.getManConRequest().getRegion();

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)
        List<House> houses = houseService.getHouseByAllConditions(allConditions, 1);

        // 1-2. 매물이 없을 경우
        if (houses == null || houses.isEmpty()) {
            houses = houseService.getHouseByAllConditions(allConditions, 2);
        }
        // 1-3. 매물이 없을 경우
        if (houses == null || houses.isEmpty()) {
            houses = houseService.getHouseByAllConditions(allConditions, 3);
        }

        // HouseWithCondition 리스트로 바꿔주기
        List<HouseWithCondition> houseWithConditions = houseWithConditionService.convertHouseList(houses);

        // 2. 공공 데이터 조건 처리
        publicDataService.injectPublicDataInList(houseWithConditions, allConditions.getPublicConditionDataList());


        // 3. 시설 조건 및 사용자 요청 위치 조건 처리
        List<IndustriesAndWeight> industriesAndWeights = industryService.injectFacilityDataInList(allConditions.getFacilityConditionDataList(), region);

        // 4. 점수 계산
        houseWithConditionService.calculate(allConditions.calculateWeightSum(), houseWithConditions, industriesAndWeights);

        // TODO: 점수가 같을 경우 다른 기준으로 어떻게든 정렬해야 함.
        // 5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬
        houseWithConditionService.sort(houseWithConditions);

        // 6. 같은 주소지의 매물을 가장 높은 순위의 것만 남기고 제거
        List<HouseWithCondition> result = houseWithConditionService.deleteDuplicates(houseWithConditions, 100);

        // 7. 100개로 sublist 및 ranking 입력

        for (int i = 0; i < result.size(); i++) {
            result.get(i).getHouse().setRanking(i + 1);
        }

        // 8. 즐겨찾기 처리
        Set<Integer> favoriteHouseIds = Optional.ofNullable(favoriteHouseRepository.findFavoriteHouseIdsByUserId(userId))
                .orElse(Collections.emptySet());
        for (HouseWithCondition houseWithCondition : result) {
            boolean isFavorite = favoriteHouseIds.contains(houseWithCondition.getHouse().getHouseId());
            houseWithCondition.setFavorite(isFavorite);
        }


        // 반환
        return result;
    }

    // 보유 데이터를 문장으로 반환
    public String conditionsToSentence() {
        StringBuilder sb = new StringBuilder();

        sb.append("보유 매물 관련 데이터: ");
        sb.append(HouseCondition.getAllData());
        sb.append("\n보유 매물 옵션 데이터: ");
        sb.append(HouseOption.getAllData());
        sb.append("\n보유 시설 데이터: ");
        sb.append(FacilityCategory.getAllData());

        return sb.toString();
    }
}

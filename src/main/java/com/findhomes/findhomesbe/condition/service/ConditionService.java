package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.*;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.RegionsRepository;
import com.findhomes.findhomesbe.service.FavoriteHouseService;
import com.findhomes.findhomesbe.service.HouseService;
import com.findhomes.findhomesbe.service.PerformanceUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.findhomes.findhomesbe.controller.MainController.ALL_CONDITIONS;

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

    public List<HouseWithCondition> exec(ManConRequest manConRequest, String gptOutput, List<String> keywords, HttpSession session, String userId) {

        // 0. gpt output 파싱해서 AllCondition 객체에 정보 넣기
        AllConditions allConditions = parsingService.parsingGptOutput(manConRequest, gptOutput, keywords);
        session.setAttribute(ALL_CONDITIONS, allConditions);
        log.info("\n===========조건 파싱 결과===========\n{}", allConditions);

        return exec2(allConditions, userId);
    }

    // 검색 기록 저장한 곳에서 바로 매물 추천 결과 가져오기 위해서 함수 따로 뺐음.
    public List<HouseWithCondition> exec2(AllConditions allConditions, String userId) {
        ManConRequest.Region region = allConditions.getManConRequest().getRegion();

        // 1. 필터링 조건으로 매물 필터링해서 매물 가져오기 (필수 조건, 매물 자체 조건, 매물 필수 옵션)
        List<House> houses = PerformanceUtil.measurePerformance(
                () -> houseService.getHouseByAllConditions(allConditions),
                "1. 필터링 조건으로 매물 필터링해서 매물 가져오기"
        );

        // HouseWithCondition 리스트로 바꿔주기
        List<HouseWithCondition> houseWithConditions = houseWithConditionService.convertHouseList(houses);
        log.info("1. 필터링 조건으로 매물 필터링해서 매물 가져오기 완료. 매물 개수: {}", houseWithConditions.size());

        // 2. 공공 데이터 조건 처리
        PerformanceUtil.measurePerformance(
                () -> publicDataService.injectPublicDataInList(houseWithConditions, allConditions.getPublicConditionDataList()),
                "2. 공공 데이터 조건 처리"
        );

        // 3. 시설 조건 및 사용자 요청 위치 조건 처리
        List<IndustriesAndWeight> industriesAndWeights = PerformanceUtil.measurePerformance(
                () -> industryService.injectFacilityDataInList(allConditions.getFacilityConditionDataList(), region),
                "3. 시설조건 및 사용자 요청 위치 조건 처리"
        );

        // 4. 점수 계산
        PerformanceUtil.measurePerformance(
                () -> houseWithConditionService.calculate(houseWithConditions, industriesAndWeights),
                "4. 점수 계산"
        );

        // TODO: 점수가 같을 경우 다른 기준으로 어떻게든 정렬해야 함.
        // 5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬
        houseWithConditionService.sort(houseWithConditions);
        log.info("5. 정렬 - houseWithConditions를 house의 score를 기준으로 내림차순으로 정렬 완료");

        // 6. 100개로 sublist 및 ranking 입력
        double preScore = -1;
        int preRanking = 0;
        for (int i = 0; i < Math.min(100, houseWithConditions.size()); i++) {
            HouseWithCondition houseWithCondition = houseWithConditions.get(i);
            double curScore = houseWithCondition.getHouse().getScore();
            if (curScore == preScore) {
                houseWithCondition.getHouse().setRanking(preRanking);
            } else {
                preRanking++;
                houseWithCondition.getHouse().setRanking(preRanking);
            }
            preScore = curScore;
        }
        for (int i = 0; i < Math.min(100, houseWithConditions.size()); i++) {
            houseWithConditions.get(i).getHouse().setRanking(i + 1);
        }

        // 7. 즐겨찾기 처리
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            boolean isFavorite = favoriteHouseService.isFavoriteHouse(userId, houseWithCondition.getHouse().getHouseId());
            houseWithCondition.setFavorite(isFavorite);
        }

        // 반환
        return houseWithConditions.subList(0, Math.min(100, houseWithConditions.size()));
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

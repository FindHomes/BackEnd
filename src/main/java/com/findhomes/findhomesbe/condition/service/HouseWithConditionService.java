package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.IndustriesAndWeight;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.industry.Industry;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HouseWithConditionService {
    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (킬로미터)

    private final HouseService houseService;

    public List<HouseWithCondition> convertHouseList(List<House> houses) {
        return houses.stream()
                .map(this::convertHouse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    private HouseWithCondition convertHouse(House house) {
        // 변환할 때, 주소 정보에서 district와 city정보 추출해서 HouseWithCondition 객체 생성 시에 넣음.
        String[] districtAndCity = houseService.extractDistrictAndCity(house.getAddress());
        if (districtAndCity == null) {
            return null;
        }
        return new HouseWithCondition(house, districtAndCity[0], districtAndCity[1]);
    }

    public HouseWithCondition findByHouseId(List<HouseWithCondition> houseWithConditions, int houseId) {
        return houseWithConditions.stream()
                .filter(hwc -> hwc.getHouse().getHouseId().equals(houseId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("해당 매물이 추천 결과에 없습니다."));
    }

    // 점수 계산
    public void calculate(List<HouseWithCondition> houseWithConditions, List<IndustriesAndWeight> industriesAndWeights) {
        // 공공 데이터 점수 계산
        calculatePublicDataScore(houseWithConditions);

        // 시설 데이터 점수 계산
        calculateFacilityDataScore(houseWithConditions, industriesAndWeights);
    }

    private void calculatePublicDataScore(List<HouseWithCondition> houseWithConditions) {
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            for (HouseWithCondition.SafetyGradeInfo safetyGradeInfo : houseWithCondition.getSafetyGradeInfoList()) {
                double score = safetyGradeInfo.getPublicData().calculateScore(safetyGradeInfo.getGrade(), safetyGradeInfo.getWeight());
                houseWithCondition.getHouse().addScore(score);
                houseWithCondition.getHouse().addPublicDataScore(score);
            }
        }
    }

    private void calculateFacilityDataScore(List<HouseWithCondition> houseWithConditions, List<IndustriesAndWeight> industriesAndWeights) {
        houseWithConditions.parallelStream()
                .forEach(houseWithCondition -> industriesAndWeights.parallelStream()
                        .forEach(industriesAndWeight -> {
                            HouseWithCondition.FacilityInfo newFacilityInfo
                                    = new HouseWithCondition.FacilityInfo(industriesAndWeight.getFacilityConditionData(), 0, 0d);
                            Integer weight = industriesAndWeight.getWeight();
                            industriesAndWeight.getIndustries().parallelStream()
                                    .forEach(industry -> {
                                        House house = houseWithCondition.getHouse();
                                        // 집과 해당 시설 간의 거리 계산
                                        double distance = calculateDistance(house.getLatitude(), house.getLongitude(), industry.getLatitude(), industry.getLongitude());
                                        if (distance <= industriesAndWeight.getMaxRadius()) {
                                            newFacilityInfo.addCount();
                                            newFacilityInfo.addDistance(distance);
                                            // 매물 하나하나에 대해 다 더하면 너무 많아서 학습률 0.1을 곱함 ㅋㅋ
                                            double score = (industriesAndWeight.getMaxRadius() - distance) * weight * 0.005;
                                            house.addScore(score);
                                            house.addFacilityDataScore(score);
                                        }
                                    });
                            houseWithCondition.getFacilityInfoList().add(newFacilityInfo);
                        }));
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 위도와 경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 하버사인 공식 계산
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 지구 반지름을 곱하여 거리 계산
        return EARTH_RADIUS_KM * c;
    }

    // 정렬
    public void sort(List<HouseWithCondition> houseWithConditions) {
        houseWithConditions.sort(Comparator.comparing(HouseWithCondition::getScore).reversed());
    }

    // 같은 주소지의 매물을 가장 높은 순위의 것만 남기고 제거
    public List<HouseWithCondition> deleteDuplicates(List<HouseWithCondition> houseWithConditions, int max) {
        Set<String> addressSet = new HashSet<>();
        List<HouseWithCondition> result = new ArrayList<>();
        int count = 0;
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            if (addressSet.add(houseWithCondition.getHouse().getAddress().split(",")[0])) {
                count++;
                result.add(houseWithCondition);
                if (count >= max) {
                    break;
                }
            }
        }

        return result;
    }
}

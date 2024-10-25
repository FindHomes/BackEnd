package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.IndustriesAndWeight;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.industry.Industry;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.service.HouseService;
import com.findhomes.findhomesbe.service.PerformanceUtil;
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
    public void calculate(int weightSum, List<HouseWithCondition> houseWithConditions, List<IndustriesAndWeight> industriesAndWeights) {
        // 공공 데이터 점수 계산
        PerformanceUtil.measurePerformance(
                () -> calculatePublicDataScore(weightSum, houseWithConditions),
                "4.1 공공 데이터 점수 계산"
        );

        // 시설 데이터 점수 계산
        PerformanceUtil.measurePerformance(
                () -> calculateFacilityDataScore(weightSum, houseWithConditions, industriesAndWeights),
                "4.1 공공 데이터 점수 계산"
        );
    }

    private void calculatePublicDataScore(int weightSum, List<HouseWithCondition> houseWithConditions) {
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            for (HouseWithCondition.SafetyGradeInfo safetyGradeInfo : houseWithCondition.getSafetyGradeInfoList()) {
                double score = safetyGradeInfo.getPublicData().calculateScore(safetyGradeInfo.getGrade(), safetyGradeInfo.getWeight() / (weightSum * 1d));
                houseWithCondition.getHouse().addScore(score);
                houseWithCondition.getHouse().addPublicDataScore(score);
            }
        }
    }

    private void calculateFacilityDataScore(int weightSum, List<HouseWithCondition> houseWithConditions, List<IndustriesAndWeight> industriesAndWeights) {
        industriesAndWeights.parallelStream()
                .forEach(industriesAndWeight -> {
                    Integer weight = industriesAndWeight.getWeight();
                    double industryMaxScore = 100.0 * weight / weightSum; // 해당 industry의 최대 점수 비율

                    // industriesAndWeight에 포함된 industries에 대해 각 houseWithCondition의 점수를 계산
                    houseWithConditions.parallelStream()
                            .forEach(houseWithCondition -> {
                                House house = houseWithCondition.getHouse();
                                HouseWithCondition.FacilityInfo newFacilityInfo
                                        = new HouseWithCondition.FacilityInfo(industriesAndWeight.getFacilityConditionData(), 0, 0d);
                                double facilityDataScore = 0;

                                // industry 점수 계산 및 합산
                                for (Industry industry : industriesAndWeight.getIndustries()) {
                                    double distance = calculateDistance(house.getLatitude(), house.getLongitude(), industry.getLatitude(), industry.getLongitude());

                                    if (distance <= industriesAndWeight.getMaxRadius()) {
                                        newFacilityInfo.addCount();
                                        newFacilityInfo.addDistance(distance);
                                        facilityDataScore += (industriesAndWeight.getMaxRadius() - distance) * weight;
                                    }
                                }

                                // 합산된 점수를 해당 집의 facilityDataScore로 설정
                                house.setFacilityDataScore(facilityDataScore);
                                houseWithCondition.getFacilityInfoList().add(newFacilityInfo);
                            });

                    // 해당 industriesAndWeight에 대해 모든 houseWithCondition의 최대 facilityDataScore 찾기
                    double maxFacilityDataScore = houseWithConditions.stream()
                            .mapToDouble(houseWithCondition -> houseWithCondition.getHouse().getFacilityDataScore())
                            .max()
                            .orElse(1.0); // 최대값이 0이 되지 않도록 기본값 설정

                    // 정규화하여 각 house의 최종 점수에 추가
                    houseWithConditions.parallelStream().forEach(houseWithCondition -> {
                        House house = houseWithCondition.getHouse();
                        double normalizedScore = (house.getFacilityDataScore() / maxFacilityDataScore) * industryMaxScore;
                        house.addScore(normalizedScore);
                    });
                });
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
            if (addressSet.add(houseWithCondition.getHouse().getAddress().split(",")[0].trim())) {
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

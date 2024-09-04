package com.findhomes.findhomesbe.condition.service;

import com.findhomes.findhomesbe.condition.domain.HouseWithCondition;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.condition.domain.IndustriesAndWeight;
import com.findhomes.findhomesbe.condition.domain.PublicData;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import com.findhomes.findhomesbe.entity.SafetyGrade;
import com.findhomes.findhomesbe.service.HouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    // 반환 전에 House 리스트로 다시 변환
    public List<House> convertToHouseList(List<HouseWithCondition> houseWithConditions) {
        return houseWithConditions.stream()
                .map(HouseWithCondition::getHouse)
                .toList();
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
        // 집 하나씩에 대해
        for (HouseWithCondition houseWithCondition : houseWithConditions) {
            // 매물 키워드 하나씩에 대해
            for (IndustriesAndWeight industriesAndWeight : industriesAndWeights) {
                Integer weight = industriesAndWeight.getWeight();
                // 매물 키워드 하나에 해당하는 여러 매물들 중 하나씩에 대해
                for (Industry industry : industriesAndWeight.getIndustries()) {
                    House house = houseWithCondition.getHouse();
                    // 집과 해당 시설 간의 거리 계산
                    double distance = calculateDistance(house.getY(), house.getX(), industry.getLatitude(), industry.getLongitude());
                    if (distance <= 3d) {
                        // 매물 하나하나에 대해 다 더하면 너무 많아서 학습률 0.1을 곱함 ㅋㅋ
                        double score = distance * weight * 0.005;
                        houseWithCondition.getHouse().addScore(score);
                        houseWithCondition.getHouse().addFacilityDataScore(score);
                    }
                }
            }
        }
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
}

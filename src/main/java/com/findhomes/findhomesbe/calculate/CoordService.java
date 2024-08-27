package com.findhomes.findhomesbe.calculate;

import com.findhomes.findhomesbe.calculate.data.FacilityCountDistance;
import com.findhomes.findhomesbe.calculate.data.HouseWithCondition;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.List;
import java.util.Map;

@Slf4j
public class CoordService {
    private static final double EARTH_RADIUS_KM = 6371.0;

    // TODO: 좌표 변환 및 경도, 위도 순서가 제대로 설정되어 있는지 확인해야함.

    public static double calculateDistance(House house, Industry industry, double radius) {
        // 위도와 경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(house.getY());
        double lon1Rad = Math.toRadians(house.getX());

//        long startTime2 = System.nanoTime();
//        Double[] industryCoord = transform(industry.getX(), industry.getY());
//        long endTime2 = System.nanoTime();
//        double lat2Rad = Math.toRadians(industryCoord[1]);
//        double lon2Rad = Math.toRadians(industryCoord[0]);
        double lat2Rad = Math.toRadians(industry.getLatitude());
        double lon2Rad = Math.toRadians(industry.getLongitude());

        // 위도와 경도의 차이 계산
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine 공식을 사용하여 거리 계산
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 거리 반환 (킬로미터 단위)
        if (EARTH_RADIUS_KM * c <= radius) {
            log.debug("탐지 완료 - 매물 번호: {}, 거리: {}", house.getHouseId(), EARTH_RADIUS_KM * c);
        }

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 매물을 radius 거리 기준으로 필터링한 후에 radius 내의 각 시설이 몇 개 있는지 카운트하고, 가장 가까운 시설의 거리를 저장합니다.
     * @param houseList 매물 리스트
     * @param industryListMap 매물 이름과 해당 이름에 해당하는 매물 리스트의 맵
     * @param radius 필터링 기준 거리
     * @return
     */
    public static List<HouseWithCondition> filterAndCalculateByFacility(List<House> houseList, Map<String, List<Industry>> industryListMap, double radius) {
        return houseList.stream()
                .filter(house -> industryListMap.entrySet().stream()
                        .allMatch(entry -> entry.getValue().stream()
                                .anyMatch(industry -> calculateDistance(house, industry, radius) <= radius)))
                .map(house -> {
                    HouseWithCondition houseWithCondition = new HouseWithCondition();
                    houseWithCondition.setHouse(house);
                    industryListMap.forEach((key, industries) -> {
                        FacilityCountDistance newFacility = new FacilityCountDistance();
                        industries.stream()
                                .map(industry -> calculateDistance(house, industry, radius))
                                .filter(distance -> distance <= radius)
                                .forEach(distance -> {
                                    newFacility.countUp();
                                    newFacility.updateMinDistance(distance);
                                });
                        houseWithCondition.getFacilityInfoMap().put(key, newFacility);
                    });
                    return houseWithCondition;
                })
                .toList();
    }
}

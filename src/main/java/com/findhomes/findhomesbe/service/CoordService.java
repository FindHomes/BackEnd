package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.entity.Industry;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CoordService {
    private static final double EARTH_RADIUS_KM = 6371.0;

    // TODO: 좌표 변환 및 경도, 위도 순서가 제대로 설정되어 있는지 확인해야함.

    public static Double[] transform(Double dblLon, Double dblLat) {
        CRSFactory factory = new CRSFactory();
        // 중부 원점 좌표계 (EPSG:2097)
        CoordinateReferenceSystem grs80 = factory.createFromName("EPSG:2097");
        // WGS84 좌표계 (EPSG:4326)
        CoordinateReferenceSystem wgs84 = factory.createFromName("EPSG:4326");
        BasicCoordinateTransform transformer = new BasicCoordinateTransform(grs80, wgs84);

        ProjCoordinate beforeCoord = new ProjCoordinate(dblLon, dblLat);
        ProjCoordinate afterCoord = new ProjCoordinate();

        transformer.transform(beforeCoord, afterCoord);

        return new Double[] {afterCoord.x, afterCoord.y};
    }

    public static double calculateDistance(House house, Industry industry, double radius) {
        // 위도와 경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(house.getY());
        double lon1Rad = Math.toRadians(house.getX());

        Double[] industryCoord = transform(industry.getX(), industry.getY());
        double lat2Rad = Math.toRadians(industryCoord[1]);
        double lon2Rad = Math.toRadians(industryCoord[0]);

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

    public static <T extends Industry> List<House> filterHouseByDistance(List<House> houseList, List<T> industryList, double radius) {
        return houseList.stream()
                .filter(house -> industryList.stream()
                        .anyMatch(industry -> calculateDistance(house, industry, radius) <= radius))
                .collect(Collectors.toList());
    }
}

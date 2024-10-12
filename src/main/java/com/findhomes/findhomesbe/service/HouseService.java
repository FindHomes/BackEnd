package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.HouseDetailResponse;
import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.repository.RegionsRepository;
import com.findhomes.findhomesbe.specification.HouseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.builder.DSL;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;
    @Autowired
    private HouseSpecification houseSpecification;
    private final RegionsRepository regionsRepository;
    public List<House> getHouseByAllConditions(AllConditions allConditions) {

        // Todo: 매물에 관한 옵션들은 Specification이 findHouseWithRegion에 쿼리문으로 추가해야할 듯 합니다.
//        return houseRepository.findAll(houseSpecification.searchHousesByAllCon(allConditions));
        ManConRequest.Region region = allConditions.getManConRequest().getRegion();
        List<House> houseList = houseRepository.findHouseWithRegion(region.getDistrict(),region.getCity(), "ACTIVE");

        log.info("0.선호지역으로 필터링된 후 매물의 개수: "+houseList.size());

        return houseList;

    }

    @Transactional
    public void saveHouse(House house) {
        Optional<House> houseOptional = houseRepository.findById(house.getHouseId());
        // 기존에 있던 매물인 경우
        if (houseOptional.isPresent()) {
            house.setCoordinate(houseOptional.get().getCoordinate());

            LocalDateTime now = LocalDateTime.now();
            if (houseOptional.get().getCreatedAt() == null) {
                house.setCreatedAt(now);
            } else {
                house.setCreatedAt(houseOptional.get().getCreatedAt());
                house.setUpdatedAt(now);
            }
        } else { // 새로 크롤링하는 매물인 경우
            Point<G2D> point = DSL.point(CoordinateReferenceSystems.WGS84, new G2D(house.getLongitude(), house.getLatitude()));
            house.setCoordinate(point);
            house.setCreatedAt(LocalDateTime.now());
        }

        houseRepository.save(house);
    }

    public String[] extractDistrictAndCity(String address) {
        String[] splitAddress = address.split(" ");
        try {
            String district = splitAddress[0];
            if (district.equals("경기도")) {
                district = "경기";
            } else if (district.contains("서울")) {
                district = "서울";
            } else if (district.contains("강원")) {
                district = "강원";
            } else if (district.equals("충청북도")) {
                district = "충북";
            } else if (district.equals("충청남도")) {
                district = "충남";
            } else if (district.equals("전라북도")) {
                district = "전북";
            } else if (district.equals("전라남도")) {
                district = "전남";
            } else if (district.equals("경상북도")) {
                district = "경북";
            } else if (district.equals("경상남도")) {
                district = "경남";
            } else if (district.contains("제주")) {
                district = "제주";
            } else if (district.contains("부산")) {
                district = "부산";
            } else if (district.contains("대구")) {
                district = "대구";
            } else if (district.contains("인천")) {
                district = "인천";
            } else if (district.contains("광주")) {
                district = "광주";
            } else if (district.contains("대전")) {
                district = "대전";
            } else if (district.contains("울산")) {
                district = "울산";
            }
            return new String[]{district, splitAddress[1]};
        } catch (Exception e) {
            // TODO: 예외 처리 제대로 해야됨.
            log.error("FacilityCategory클래스 extractDistrictAndCity함수", e);
            return null;
        }
    }


    public House getHouse(int houseId) {
        // 매물 정보 조회
        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            throw new DataNotFoundException("입력된 id에 해당하는 매물이 없습니다");
        }
        return house;
    }
}

package com.findhomes.findhomesbe.service;

import com.findhomes.findhomesbe.DTO.ManConRequest;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.condition.domain.AllConditions;
import com.findhomes.findhomesbe.entity.House;
import com.findhomes.findhomesbe.repository.HouseRepository;
import com.findhomes.findhomesbe.repository.RegionsRepository;
import com.findhomes.findhomesbe.specification.HouseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HouseService {
    private final HouseRepository houseRepository;
    @Autowired
    private HouseSpecification houseSpecification;
    private final RegionsRepository regionsRepository;
    public List<House> getHouseByAllConditions(AllConditions allConditions) {

//        return houseRepository.findAll(houseSpecification.searchHousesByAllCon(allConditions));
        String city = allConditions.getManConRequest().getRegion().getCity();
        List<House> houseList = houseRepository.findHouseWithRegion(city);
        log.info("선호지역으로 필터링된 후 매물의 개수: "+houseList.size());
        return houseList;

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
}
